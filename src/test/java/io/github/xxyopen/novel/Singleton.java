package io.github.xxyopen.novel;

/**
 * @author Lenovo
 * @date 2023-05-09 14:38
 */
public class Singleton {
    private volatile static  Singleton singleton;

    private Singleton(){}
    public Singleton getSingleton() {
        if (singleton == null) {
            synchronized (Singleton.class) {
                if (singleton == null) {
                    singleton = new Singleton();
                }
            }
        }
        return singleton;
    }

}
