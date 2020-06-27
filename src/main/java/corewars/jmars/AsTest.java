/*-
 * Copyright (c) 1998 Brian Haskin jr.
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
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
/**
 * reads in the file specified on the command line and
 * produces a listing.
 */
package corewars.jmars;

import corewars.jmars.assembler.Assembler;
import corewars.jmars.assembler.AssemblerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AsTest {

    public static void main(String[] args) {

        Result<FileInputStream> resultFile = getFileStreamFromArgs(args);

        if (!resultFile.isOk()) {
            System.out.println(resultFile.getMessage());
            return;
        }

        Result<String> resultOutput = createOutput(resultFile.getValue());

        if (!resultOutput.isOk()) {
            System.out.println(resultOutput.getMessage());
            return;
        }

        System.out.println(resultOutput.getMessage());
    }

    private static Result<String> createOutput(FileInputStream file) {

        StringBuilder sb = new StringBuilder();


        try {
            Assembler parser = new corewars.jmars.assembler.icws94p.ICWS94p();

            parser.parseWarrior(file);

            sb.append(";name " + parser.getName() + "\n");
            sb.append(";author " + parser.getAuthor() + "\n");
            sb.append("ORG	" + parser.getStart() + "\n");

            Memory[] warrior = parser.getWarrior();

            for (int i = 0; i < warrior.length; i++) {
                sb.append(warrior[i] + "\n");
            }

        } catch (AssemblerException ae) {
            return new Result<>(false, "Error parsing warrior file.\n" + ae.toString());
        } catch (IOException ioe) {
            return new Result<>(false, ioe.toString());
        }

        return new Result<>(true, sb.toString());
    }

    private static Result<FileInputStream> getFileStreamFromArgs(String[] args) {
        if (args.length != 1) {
            return new Result<>(false, "usage: AsTest filename");
        }

        File file = new File(args[0]);

        if (!file.exists()) {
            return new Result<>(false, "File " + file.getAbsolutePath() + " doesn't exist.");
        }

        FileInputStream fileStream;

        try {
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return new Result<>(false, "file could not be opened");
        }

        return new Result<>(true, fileStream);
    }
}
