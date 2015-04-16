package org.memnon.server;

import org.httpkit.PrefixThreadFactory;
import org.httpkit.server.*;

import java.util.concurrent.*;

/**
 * User: melon
 * Date: 14-12-25
 * Time: 下午3:42
 */
class MultiThreadHandler implements IHandler {
    private ExecutorService execs;

    public MultiThreadHandler(int thread, String prefix, int queueSize) {
        PrefixThreadFactory factory = new PrefixThreadFactory(prefix);
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
        execs = new ThreadPoolExecutor(thread, thread, 0, TimeUnit.MILLISECONDS, queue, factory);
    }

    public void close(int timeoutMs) {
        if (timeoutMs > 0) {
            execs.shutdown();
            try {
                if (!execs.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                    execs.shutdownNow();
                }
            } catch (InterruptedException ie) {
                execs.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } else {
            execs.shutdownNow();
        }
    }

    @Override
    public void handle(AsyncChannel channel, Frame frame) {

    }

    public void handle(HttpRequest request, final RespCallback callback) {
        execs.submit(new HttpHandler(request, callback));
    }

    public void clientClose(AsyncChannel channel, int status) {
    }
}
