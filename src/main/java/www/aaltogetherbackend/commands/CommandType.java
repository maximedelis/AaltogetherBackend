package www.aaltogetherbackend.commands;

public enum CommandType {
    PLAY(CommandType.PLAY_BEAN_NAME),
    PAUSE(CommandType.PAUSE_BEAN_NAME),
    SEEK_TO(CommandType.SEEK_TO_BEAN_NAME),
    STOP(CommandType.STOP_BEAN_NAME),
    SYNC(CommandType.SYNC_BEAN_NAME),
    OPEN(CommandType.OPEN_BEAN_NAME),
    KICK(CommandType.KICK_BEAN_NAME),
    END(CommandType.END_BEAN_NAME);

    public static final String PLAY_BEAN_NAME = "playHandler";
    public static final String PAUSE_BEAN_NAME = "pauseHandler";
    public static final String SEEK_TO_BEAN_NAME = "seekToHandler";
    public static final String STOP_BEAN_NAME = "stopHandler";
    public static final String SYNC_BEAN_NAME = "syncHandler";
    public static final String OPEN_BEAN_NAME = "openHandler";
    public static final String KICK_BEAN_NAME = "kickHandler";
    public static final String END_BEAN_NAME = "endHandler";

    private final String beanName;
    CommandType(String beanName) { this.beanName = beanName; }
    public String beanName() { return beanName; }
}
