package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.provider.ServiceProvider;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 并发版 RPC 服务端引擎。
 * 整合 ServiceProvider 容器，支持多业务接口的统一监听与并行处理。
 * * @author jiejie
 */
public class RpcServer {

    private final ExecutorService threadPool;

    public RpcServer() {
        // 初始化自定义参数线程池，保障系统资源的合理分配与回收
        this.threadPool = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory()
        );
    }

    /**
     * 开启服务监听循环
     * @param serviceProvider 服务注册中心实例，包含所有已挂载的业务接口
     * @param port 监听的 TCP 端口
     */
    public void start(ServiceProvider serviceProvider, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("【V4.0 Server】RPC 服务容器已启动，监听端口: " + port);

            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                // 委派工作线程处理 I/O 任务
                threadPool.execute(new RequestHandlerThread(socket, serviceProvider));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}