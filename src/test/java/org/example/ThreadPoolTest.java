package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class ThreadPoolTest {
    private ThreadPool threadPool;
    private AtomicInteger counter;

    @BeforeEach
    void setUp() {
        threadPool = new ThreadPool(3); // пул с тремя потоками
        counter = new AtomicInteger(0); // счетчик выполненных задач
    }

    @AfterEach
    void tearDown() {
        threadPool.shutdown();
        threadPool.awaitTermination();
    }

    @Test
    void testExecuteTasks() {
        // Добавили 10 задач, каждая увеличивает счетчик
        for (int i = 0; i < 10; i++) {
            threadPool.execute(() -> {
                counter.incrementAndGet();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination();

        // Проверяем, что все задачи выполнены
        assertEquals(10, counter.get(), "Все задачи должны быть выполнены.");
    }

    @Test
    void testShutdownPreventsNewTasks() {
        threadPool.shutdown();

        // При попытке добавить новую задачу получим IllegalStateException
        assertThrows(IllegalStateException.class, () -> threadPool.execute(() -> {}),
                "После вызова shutdown пул не должен принимать новые задачи.");
    }

    @Test
    void testAwaitTermination() {
        // Добавили задачи, которые увеличивают счетчик
        for (int i = 0; i < 5; i++) {
            threadPool.execute(() -> {
                counter.incrementAndGet();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination();

        // Проверяем, что после awaitTermination() все задачи выполнены
        assertEquals(5, counter.get(), "Все задачи должны быть выполнены до завершения пула.");
    }
}