/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package com.sun.jna;

import java.lang.ref.WeakReference;
import junit.framework.TestCase;

public class MemoryTest extends TestCase {
    public void testAutoFreeMemory() throws Exception {
        Memory core = new Memory(10);
        Pointer shared = core.share(0, 5);
        WeakReference ref = new WeakReference(core);
        
        core = null;
        System.gc();
        long start = System.currentTimeMillis();
        assertNotNull("Memory prematurely GC'd", ref.get());
        shared = null;
        System.gc();
        while (ref.get() != null) {
            if (System.currentTimeMillis() - start > 5000)
                break;
            Thread.sleep(10);
        }
        assertNull("Memory not GC'd", ref.get());
    }

    public void testSharedMemoryBounds() {
        Memory base = new Memory(16);
        Pointer shared = base.share(4, 4);
        shared.getInt(-4);
        try {
            shared.getInt(-8);
            fail("Bounds check should fail");
        }
        catch(IndexOutOfBoundsException e) {
        }
        shared.getInt(8);
        try {
            shared.getInt(12);
            fail("Bounds check should fail");
        }
        catch(IndexOutOfBoundsException e) {
        }
    }

    public void testAlignment() {
        final int SIZE = 128;
        Memory base = new Memory(SIZE);
        for (int align=1;align < 8;align *= 2) {
            Memory unaligned = base;
            long mask = ~((long)align - 1);
            if ((base.peer & mask) == base.peer)
                unaligned = (Memory)base.share(1, SIZE-1);
            Pointer aligned = unaligned.align(align);
            assertEquals("Memory not aligned",
                         aligned.peer & mask, aligned.peer);
        }
        try {
            base.align(-1);
            fail("Negative alignments not allowed");
        }
        catch(IllegalArgumentException e) { }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MemoryTest.class);
    }
}
