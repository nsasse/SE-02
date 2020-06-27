/*
 * Copyright (c) 2000 Anton Marsden
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package corewars.jmars.marsVM;

public class SimpleInstruction implements Instruction {

    protected InstructionOpcode opcode;
    protected InstructionModifier modifier;
    protected InstructionMode amode;
    protected InstructionMode bmode;
    protected ModularValue avalue;
    protected ModularValue bvalue;

    public SimpleInstruction() {
        opcode = new InstructionOpcode();
        modifier = new InstructionModifier();
        amode = new InstructionMode();
        bmode = new InstructionMode();
        avalue = new ModularValue();
        bvalue = new ModularValue();
    }

    public SimpleInstruction(Instruction instr) {
        opcode = new InstructionOpcode();
        modifier = new InstructionModifier();
        amode = new InstructionMode();
        bmode = new InstructionMode();
        avalue = new ModularValue();
        bvalue = new ModularValue();
        copy(instr);
    }

    public byte getOpcode() {
        return opcode.get();
    }

    public byte getModifier() {
        return modifier.get();
    }

    public byte getAMode() {
        return amode.get();
    }

    public byte getBMode() {
        return bmode.get();
    }

    public void setOpcode(byte op) {
        opcode.set(op);
    }

    public void setModifier(byte mod) {
        modifier.set(mod);
    }

    public void setAMode(byte mode) {
        amode.set(mode);
    }

    public void setBMode(byte mode) {
        bmode.set(mode);
    }

    public void copy(Instruction instr) {
        opcode.set(instr.getOpcode());
        modifier.set(instr.getModifier());
        amode.set(instr.getAMode());
        bmode.set(instr.getBMode());
        avalue.copy(instr.aValue());
        bvalue.copy(instr.bValue());
    }

    public ModularValue aValue() {
        return avalue;
    }

    public ModularValue bValue() {
        return bvalue;
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        tmp.append(opcode.toString() + "." + modifier.toString());
        while (tmp.length() < 7) {
            tmp.append(" ");
        }
        tmp.append(amode.toString()
                + avalue.toString()
                + ", "
                + bmode.toString()
                + bvalue.toString());

        return tmp.toString();
    }
}
