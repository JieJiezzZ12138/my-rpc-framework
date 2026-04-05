package com.jiejie.rpc.core.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 并发增强版 RPC 服务端。
 * 采用 ThreadPoolExecutor 管理工作线程，支持多客户端同时发起请求。
 * * @author jiejie
 */
public class RpcServer {

    private final ExecutorService threadPool;

    /**
     * 初始化服务端，配置线程池核心参数。
     * 参数选型依据：核心 5 线程，最大 10 线程，队列容量 100。
     */
    public RpcServer() {
        int corePoolSize = 5;
        int maximumPoolSize = 10;
        long keepAliveTime = 60;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // 显式创建线程池，避免使用 Executors 的快捷方法以防资源耗尽
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workingQueue,
                threadFactory
        );
    }

    /**
     * 启动服务监听。
     * * @param service 本地服务实现实例
     * @param port    监听端口
     */
    public void start(Object service, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("RPC Server V3.0 (Concurrent) is running on port: " + port);

            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                // 将新连接封装为处理任务，提交至线程池执行
                threadPool.execute(new RequestHandlerThread(socket, service));
            }
        } catch (Exception e) {
            System.err.println("Server encountered an exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 优雅关闭线程池
            threadPool.shutdown();
        }
    }
}