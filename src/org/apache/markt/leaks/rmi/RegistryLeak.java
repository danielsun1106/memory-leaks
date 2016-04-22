package org.apache.markt.leaks.rmi;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Demonstrates the correct way for a web application created RMI registry to be
 * closed down, thereby avoiding a memory leak.
 * <p>
 * TODO: Figure out how to identify a web application created registry so it
 *       can be shut down by the container if the web application fails to do
 *       so. We need:
 *       <ul>
 *       <li>The current registry list</li>
 *       <li>A way to determine TCCL for each registry</li>
 *       </ul>
 */
public class RegistryLeak {

    public static void main(String[] args) {

        RegistryLeak registryLeak = new RegistryLeak();

        // Switch TCCL
        registryLeak.start();

        // Create RMI registry
        registryLeak.register();

        // Clean-up registry
        registryLeak.deregister();

        // Restore TCCL
        registryLeak.stop();

        // Check for leaks
        int count = 0;
        while (count < 10 && registryLeak.leakCheck()) {
            // Trigger GC
            System.gc();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        System.out.println("There were " + count + " calls to GC");

        if (registryLeak.leakCheck()) {
            System.out.println("Leak");
        } else {
            System.out.println("No leak");
        }
    }

    private static final ClassLoader ORIGINAL_CLASS_LOADER =
            Thread.currentThread().getContextClassLoader();

    private WeakReference<ClassLoader> moduleClassLoaderRef;
    private Registry registry;


    private void start() {
        ClassLoader moduleClassLoader = new URLClassLoader(new URL[] {}, ORIGINAL_CLASS_LOADER);

        Thread.currentThread().setContextClassLoader(moduleClassLoader);
        moduleClassLoaderRef = new WeakReference<>(moduleClassLoader);
    }


    private void register() {
        try {
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void deregister() {
        try {
            UnicastRemoteObject.unexportObject(registry, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stop() {
        Thread.currentThread().setContextClassLoader(ORIGINAL_CLASS_LOADER);
    }


    private boolean leakCheck() {
        return moduleClassLoaderRef.get() != null;
    }
}