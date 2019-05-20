package network;

/**
 * Contains the types of messages that can be sent from clients. If any message type turns out to be un-parsable
 * the default value of UNKNOWN_MESSAGE_FORMAT will be used
 * @author Logan Earl
 */
public enum ServerMessageType implements WebServer.MessageType {
    /**Message sent from server to ask the client for information. ServerPromptMessage*/
    SERVER_PROMPT_MESSAGE("ServerPromptMessage"),


    /**message sent by the client to let the server know it is there. ClientGreeting*/
    CLIENT_GREETING("connect"),
    /**Type used to denote an attempted login from a client. May or may not succeed. ClientLoginAttempt*/
    CLIENT_LOGIN_MESSAGE("login"),
    /**Simple text message used for debugging purposes. DebugMessage*/
    CLIENT_DEBUG_MESSAGE("debug"),
    /**Message used to update account information. Also used to create new accounts. CLIENT_UPDATE_ACCOUNT_MESSAGE*/
    CLIENT_ACCOUNT_UPDATE_MESSAGE("update"),
    /**Message sent from the client to elevate the permission level of a different user*/
    CLIENT_ELEVATE_USER_MESSAGE("elevate"),
    /**Message sent from the user to log themselves or others out. Can only log out users with lesser permission level*/
    CLIENT_LOGOUT_MESSAGE("logout"),


    /**The default type of messages that turned out to be un-parsable. UnknownMessageFormat*/
    UNKNOWN_MESSAGE_FORMAT("UnknownMessageFormat");

    private String messageType;

    ServerMessageType(String parsableFormat) {
        this.messageType = parsableFormat;
    }

    public String getParsableFormat() {
        return messageType;
    }

    /**
     * Pass in the message type header from a client message and this method will attempt to determine the message type
     * @param toParse the message type from the message header
     * @return the type of the message or UNKNOWN_MESSAGE_FORMAT if it could not be determined
     */
    public static ServerMessageType parseFromString(String toParse) {
        for (ServerMessageType m : ServerMessageType.values())
            if (m.messageType.equals(toParse.toLowerCase()))
                return m;
        return UNKNOWN_MESSAGE_FORMAT;
    }
}