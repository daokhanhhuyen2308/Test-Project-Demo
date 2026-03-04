package com.august.file.strategy;

import com.august.protocol.profile.FilePurpose;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FileStrategyContext {
    private final Map<FilePurpose, FileStrategy> strategies;

    public FileStrategyContext(List<FileStrategy> strategyList){
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FileStrategy::getPurpose, Function.identity()));
    }

    public FileStrategy getStrategy(FilePurpose purpose){
        return strategies.get(purpose);
    }
}
