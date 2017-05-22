/*
 * Copyright (c) 2017 VMware Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.hiero.sketch;

import org.hiero.storage.CsvFileReader;
import org.hiero.storage.CsvFileWriter;
import org.hiero.table.*;
import org.hiero.table.api.ContentsKind;
import org.hiero.table.api.IColumn;
import org.hiero.table.api.ITable;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CsvReaderTest {
    static final String dataFolder = "../data";
    static final String csvFile = "On_Time_Sample.csv";
    static final String schemaFile = "On_Time.schema";

    @Nullable
    ITable readTable(String folder, String file) throws IOException {
        Path path = Paths.get(folder, file);
        CsvFileReader.CsvConfiguration config = new CsvFileReader.CsvConfiguration();
        config.allowFewerColumns = false;
        config.hasHeaderRow = true;
        config.allowMissingData = false;
        CsvFileReader r = new CsvFileReader(path, config);
        return r.read();
    }

    @Test
    public void readCsvFileTest() throws IOException {
        ITable t = this.readTable(dataFolder, csvFile);
        Assert.assertNotNull(t);
    }

    @Test
    public void readCsvFileWithSchemaTest() throws IOException {
        Path path = Paths.get(dataFolder, schemaFile);
        Schema schema = Schema.readFromJsonFile(path);
        path = Paths.get(dataFolder, csvFile);
        CsvFileReader.CsvConfiguration config = new CsvFileReader.CsvConfiguration();
        config.allowFewerColumns = false;
        config.hasHeaderRow = true;
        config.allowMissingData = false;
        config.schema = schema;
        CsvFileReader r = new CsvFileReader(path, config);
        ITable t = r.read();
        Assert.assertNotNull(t);
        /*
        System.gc();
        long mem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        System.out.printf("Total memory %d, Free memory %d.", mem, freeMem);*/
    }

    void writeReadTable(ITable table) throws IOException {
        UUID uid = UUID.randomUUID();
        String tmpFileName = uid.toString();
        Path path = Paths.get(".", tmpFileName);

        try {
            CsvFileWriter writer = new CsvFileWriter(path);
            writer.setWriteHeaderRow(true);
            writer.writeTable(table);

            CsvFileReader.CsvConfiguration config = new CsvFileReader.CsvConfiguration();
            config.allowFewerColumns = false;
            config.hasHeaderRow = true;
            config.allowMissingData = false;
            config.schema = table.getSchema();
            CsvFileReader r = new CsvFileReader(path, config);
            ITable t = r.read();
            Assert.assertNotNull(t);

            List<String> list = Files.readAllLines(path);
            for (String l : list)
                System.out.println(l);

            String ft = table.toLongString(table.getNumOfRows());
            String st = t.toLongString(t.getNumOfRows());
            Assert.assertEquals(ft, st);
        } finally {
            if (Files.exists(path))
                Files.delete(path);
        }
    }

    @Test
    public void writeSmallFileTest() throws IOException {
        ColumnDescription nulls = new ColumnDescription("AllNulls", ContentsKind.String, true);
        StringListColumn first = new StringListColumn(nulls);
        first.append(null);
        first.append(null);
        ColumnDescription empty = new ColumnDescription("AllEmpty", ContentsKind.String, true);
        StringListColumn second = new StringListColumn(empty);
        second.append("");
        second.append("");
        ColumnDescription integers = new ColumnDescription("Integers", ContentsKind.Integer, true);
        IntListColumn third = new IntListColumn(integers);
        third.append(0);
        third.appendMissing();
        List<IColumn> cols = new ArrayList<IColumn>();
        cols.add(first);
        cols.add(second);
        cols.add(third);
        ITable table = new Table(cols);
        writeReadTable(table);
    }

    @Test
    public void writeCsvFileTest() throws IOException {
        ITable tbl = this.readTable(dataFolder, csvFile);
        writeReadTable(tbl);
    }
}


