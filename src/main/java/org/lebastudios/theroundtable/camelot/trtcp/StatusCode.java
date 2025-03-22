package org.lebastudios.theroundtable.camelot.trtcp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public enum StatusCode implements FromBytes<StatusCode>, IntoBytes
{
    OK, GENERIC_ERROR, NEED_CONNECTION, INTERNAL_SERVER_ERROR, ALREADY_CONNECTED, INVALID_REQUEST, EVENT_NOT_FOUND,
    LISTENER_NOT_FOUND, EVENT_ALREADY_EXISTS, ALREADY_SUBSCRIBED;

    @Override
    public StatusCode fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length != 1) throw new ParseException("Expected exactly one byte", 0);
        
        return switch (bytes[0])
        {
            case 0 -> OK;
            case -1 -> GENERIC_ERROR;
            case -2 -> NEED_CONNECTION;
            case -3 -> INTERNAL_SERVER_ERROR;
            case 1 -> ALREADY_CONNECTED;
            case 2 -> INVALID_REQUEST;
            case 3 -> EVENT_NOT_FOUND;
            case 4 -> LISTENER_NOT_FOUND;
            case 5 -> EVENT_ALREADY_EXISTS;
            case 6 -> ALREADY_SUBSCRIBED;
            default -> throw new ParseException("Invalid status code", 0);
        };
    }

    @Override
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>();
        
        bytes.add((byte) switch (this) {
            case OK -> 0;
            case GENERIC_ERROR -> -1;
            case NEED_CONNECTION -> -2;
            case INTERNAL_SERVER_ERROR -> -3;
            case ALREADY_CONNECTED -> 1;
            case INVALID_REQUEST -> 2;
            case EVENT_NOT_FOUND -> 3;
            case LISTENER_NOT_FOUND -> 4;
            case EVENT_ALREADY_EXISTS -> 5;
            case ALREADY_SUBSCRIBED -> 6;
        });
        
        return bytes;
    }
}
