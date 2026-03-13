package com.august.profile.service.impl;

import com.august.profile.dto.ProfileResponse;
import com.august.sharecore.dto.SearchAfterCursor;
import com.august.profile.dto.UserFollowRequestFilter;
import com.august.profile.dto.UserSummaryResponse;
import com.august.profile.entity.UserFollow;
import com.august.profile.entity.UserProfile;
import com.august.profile.enums.Action;
import com.august.profile.mapper.ProfileMapper;
import com.august.profile.record.FollowEvent;
import com.august.profile.repository.UserFollowRepository;
import com.august.profile.repository.UserProfileRepository;
import com.august.profile.service.UserFollowService;
import com.august.sharecore.dto.PageResponse;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowServiceImpl implements UserFollowService {
    private static final String FOLLOWER_KEY = "user:followers:";
    private static final String FOLLOWING_KEY = "user:followings:";
    private final StringRedisTemplate stringRedisTemplate;
    private final UserProfileRepository userProfileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ProfileMapper profileMapper;
    private final UserFollowRepository userFollowRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> listFollowers(UserFollowRequestFilter filter) {
        return getFollowPage(filter, (lastCreatedAd, lastId) ->
        userFollowRepository.findFollowersBySearchAfter(filter.getAuthorId(), lastCreatedAd, lastId,
                PageRequest.of(filter.getPage(), filter.getSize() + 1)),
        UserFollow::getFollowerId);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserSummaryResponse> listFollowings(UserFollowRequestFilter filter) {
        return getFollowPage(filter, (lastCreatedAt, lastId)
                -> userFollowRepository.findFollowingsBySearchAfter(filter.getAuthorId(), lastCreatedAt, lastId,
                PageRequest.of(filter.getPage(), filter.getSize() + 1)),
                UserFollow::getFollowingId);
    }

    @Override
    @Transactional
    public ProfileResponse toggleFollow(String username, String currentUserId) {

        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new AppCustomException(ErrorCode.PROFILE_NOT_FOUND_BY_USERNAME, username));

        String authorKeycloakId = profile.getKeycloakId();

        String myFollowingKey = FOLLOWING_KEY + currentUserId;
        String authorFollowerKey = FOLLOWER_KEY + authorKeycloakId;
        boolean isFollowing;
        Action action;

        //check
        Boolean isMemberFollowing = stringRedisTemplate.opsForSet().isMember(myFollowingKey, authorKeycloakId);

        if (Boolean.TRUE.equals(isMemberFollowing)){
            stringRedisTemplate.opsForSet().remove(myFollowingKey, authorKeycloakId);
            stringRedisTemplate.opsForSet().remove(authorFollowerKey, currentUserId);
            isFollowing = false;
            log.info("Unfollowing author by id: {}", authorKeycloakId);
            action = Action.UNFOLLOW;
        }
        else {
            stringRedisTemplate.opsForSet().add(myFollowingKey, authorKeycloakId);
            stringRedisTemplate.opsForSet().add(authorFollowerKey, currentUserId);
            isFollowing = true;
            log.info("Following author by id: {}", authorKeycloakId);
            action = Action.FOLLOW;
        }

        Long followingCount = stringRedisTemplate.opsForSet().size(authorFollowerKey);
        Long followerCount = stringRedisTemplate.opsForSet().size(FOLLOWING_KEY + authorKeycloakId);

        eventPublisher.publishEvent(new FollowEvent(currentUserId, authorKeycloakId, action));

        ProfileResponse response = profileMapper.mapToResponse(profile);
        response.setIsFollowing(isFollowing);
        response.setFollowingCount(Optional.ofNullable(followingCount).orElse(0L));
        response.setFollowerCount(Optional.ofNullable(followerCount).orElse(0L));

        return response;
    }


    @Transactional
    @Override
    public void upsertFollow(String followerId, String followingId) {
        userFollowRepository.upsertFollow(followerId, followingId);
    }

    @Transactional
    @Override
    public void deleteByFollowerIdAndFollowingId(String followerId, String followingId) {
        userFollowRepository.deleteFollow(followerId, followingId);
    }

    private void validateProfileExists(String authorId) {
        if (!userProfileRepository.existsById(authorId)) {
            throw new AppCustomException(ErrorCode.PROFILE_NOT_FOUND_BY_ID, authorId);
        }
    }

    private PageResponse<UserSummaryResponse> buildPageResponse(String currentProfileId,
                                                                List<UserFollow> results,
                                                                List<String> targetProfileIds,
                                                                SearchAfterCursor nextSearchAfter,
                                                                boolean hasMore){
       if (targetProfileIds.isEmpty()){
           return PageResponse.<UserSummaryResponse>builder()
                   .pageSize(results.size())
                   .content(Collections.emptyList())
                   .nextSearchAfter(SearchAfterCursor.builder().lastCreatedAt(null).lastId(null).build())
                   .hasMore(false)
                   .build();
       }

       List<UserProfile> userProfiles = userProfileRepository.findAllById(targetProfileIds);

        Map<String, UserProfile> profileMap = userProfiles
                .stream().collect(Collectors.toMap(UserProfile::getId, userProfile -> userProfile));

        Set<String> followedProfileIdsByCurrentUser = userFollowRepository
                .findFollowingIdsByFollowerIdAndFollowingIdIn(currentProfileId, targetProfileIds);

        List<UserSummaryResponse> responses =  targetProfileIds
                .stream().map(profileMap::get)
                .filter(Objects::nonNull)
                .map(profile -> UserSummaryResponse.builder()
                        .profileId(profile.getId())
                        .username(profile.getUsername())
                        .avatarUrl(profile.getAvatarUrl())
                        .bio(profile.getBio())
                        .isFollowing(followedProfileIdsByCurrentUser.contains(profile.getId()))
                        .build())
                .toList();

        return PageResponse.<UserSummaryResponse>builder()
                .content(responses)
                .pageSize(results.size())
                .nextSearchAfter(hasMore
                        ?  SearchAfterCursor.builder().lastCreatedAt(nextSearchAfter.getLastCreatedAt())
                        .lastId(nextSearchAfter.getLastId())
                        .build()
                        : null)
                .hasMore(hasMore)
                .build();

    }

    public PageResponse<UserSummaryResponse> getFollowPage(UserFollowRequestFilter filter,
                                                           BiFunction<Instant, String, List<UserFollow>> repoCallBack,
                                                           Function<UserFollow, String> idExtract){
        String currentProfileId = getProfileIdBasedOnKeycloakId(filter.getCurrentKeycloakId());

        validateProfileExists(filter.getAuthorId());

        SearchAfterCursor searchAfterCursor = filter.getSearchAfterCursor();

        Instant lastCreatedAt = null;
        String lastId = null;

        if (searchAfterCursor != null){
            lastCreatedAt = (Instant) searchAfterCursor.getLastCreatedAt();
            lastId = searchAfterCursor.getLastId();
        }

        List<UserFollow> results = repoCallBack.apply(lastCreatedAt, lastId);

        boolean hasMore = results.size() > filter.getSize();

        if (hasMore){
            results = results.subList(0, filter.getSize());
        }

        List<String> targetIds = results.stream()
                .map(idExtract)
                .toList();

        SearchAfterCursor nextSearchAfter = null;

        if (hasMore && !results.isEmpty()) {
            UserFollow lastFollow = results.getLast();
            nextSearchAfter = SearchAfterCursor.builder()
                    .lastCreatedAt(lastFollow.getCreatedAt())
                    .lastId(String.valueOf(lastFollow.getId()))
                    .build();
        }

        return buildPageResponse(currentProfileId, results, targetIds, nextSearchAfter, hasMore);
    }

    private String getProfileIdBasedOnKeycloakId(String currentUserId){
        return userProfileRepository.findByKeycloakId(currentUserId)
                .getId();
    }

}
