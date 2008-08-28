package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;
import java.util.NoSuchElementException;

public final class CharacterWalker
{

    public CharacterWalker(CharacterSet characterset, byte abyte0[], int i, int j)
    {
        charSet = characterset;
        bytes = abyte0;
        next = i;
        end = i + j;
        if(next < 0)
            next = 0;
        if(end > abyte0.length)
            end = abyte0.length;
    }

    public int nextCharacter()
        throws NoSuchElementException
    {
        try{
        return charSet.decode(this);
        }catch(SQLException sqlexception){
            throw new NoSuchElementException(sqlexception.getMessage());
        }
//        SQLException sqlexception;
//        sqlexception;
//        throw new NoSuchElementException(sqlexception.getMessage());
    }

    public boolean hasMoreCharacters()
    {
        return next < end;
    }

    CharacterSet charSet;
    byte bytes[];
    int next;
    int end;
    int shiftstate;
}