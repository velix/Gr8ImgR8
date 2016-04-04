package Workers;

interface WorkerInterface {

    void initialize(int port);
    void waitForTasksThread();
}
