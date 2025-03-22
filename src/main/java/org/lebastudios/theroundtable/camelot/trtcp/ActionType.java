package org.lebastudios.theroundtable.camelot.trtcp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public enum ActionType implements FromBytes<ActionType>, IntoBytes
{
    CONNECT, LISTEN, INVOKE, CREATE, LEAVE, CALLBACK, ;

    @Override
    public ActionType fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length != 1) throw new ParseException("ActionType byte array is not of size 1", 0);
        
        return ActionType.values()[bytes[0]];
    }

    @Override
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>();
        
        bytes.add((byte) this.ordinal());
        
        return bytes;
    }
}
