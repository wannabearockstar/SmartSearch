package com.wannabe.smartsearch.services;

import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newFixedThreadPool;

abstract class FileCrawlerTrimService implements Function<String, String> {

    private static final Logger logger = Logger.getInstance(FileCrawlerTrimService.class);
    private static final ExecutorService executor = newFixedThreadPool(2);
    private Future<Collection<String>> names;

    FileCrawlerTrimService(final Supplier<Collection<String>> classNamesGetter, @Nullable CompilerManager compilerManager) {
        names = executor.submit(classNamesGetter::get);

        if (compilerManager == null) {
            logger.warn("Cant register listener for compiler manager: compile manager is null");
            return;
        }
        compilerManager.addCompilationStatusListener(new CompilationStatusListener() {
            @Override
            public void compilationFinished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                names = executor.submit(classNamesGetter::get);
            }

            @Override
            public void fileGenerated(String outputRoot, String relativePath) {
                //do nothing
            }
        });

    }

    final String removeFaceContent(@NotNull String data, Function<String, String> regexpGetter) {
        try {
            Predicate<String> remainWord = names.get().stream()
                    .map(name -> Pattern.compile(regexpGetter.apply(name)).asPredicate())
                    .reduce(Predicate::or).orElse(s -> false).negate();
            return stream(data.split("\\s+"))
                    .filter(remainWord)
                    .reduce((s, s2) -> s + " " + s2).orElse("");
        } catch (InterruptedException e) {
            names.cancel(true);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            return data;
        }
        return data;
    }
}
