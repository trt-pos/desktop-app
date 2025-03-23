package org.lebastudios.theroundtable.camelot.trtcp;

import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

import java.text.ParseException;

public enum ActionType implements FromBytes<ActionType>, IntoBytes
{
    CONNECT, LISTEN, INVOKE, CREATE, LEAVE, CALLBACK,
    ;

    @Override
    public ActionType fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length != 1) throw new ParseException("ActionType byte array is not of size 1", 0);

        return ActionType.values()[bytes[0]];
    }

    @Override
    public byte[] intoBytes()
    {
        return new byte[]{(byte) this.ordinal()};
    }
}
