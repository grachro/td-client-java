//
// Java Client Library for Treasure Data Cloud
//
// Copyright (C) 2011 - 2013 Muga Nishizawa
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.treasure_data.model;

import java.util.ArrayList;
import java.util.List;

public class TableSchema extends AbstractModel {
    private static interface Type {
    }

    private static interface ContainerType extends Type {
        String getContainerType();
    }

    private static class ArrayType implements ContainerType {
        private Type elementType;

        private ArrayType(Type elementType) {
            this.elementType = elementType;
        }

        public String getContainerType() {
            return "array";
        }

        @Override
        public String toString() {
            return getContainerType() + "<" + elementType.toString() + ">";
        }
    }

    private static class PrimitiveType implements Type {
        private static final PrimitiveType STRING = new PrimitiveType("string");
        private static final PrimitiveType INT = new PrimitiveType("int");
        private static final PrimitiveType LONG = new PrimitiveType("long");
        private static final PrimitiveType DOUBLE = new PrimitiveType("double");

        private String type;

        private PrimitiveType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return getType();
        }
    }

    public static class Pair {
        String col;
        Type type;

        Pair(String col, Type type) {
            this.col = col;
            this.type = type;
        }

        public String getColumnName() {
            return col;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return col + ":" + type;
        }
    }

    private static Pair parsePair(String pairString) {
        String[] pair = pairString.split(":");

        if (pair.length != 2) {
            throw new IllegalArgumentException(""); // TODO
        }

        return new Pair(pair[0], parseType(pair[1]));
    }

    private static Type parseType(String typeString) {
        if (PrimitiveType.STRING.getType().equals(typeString)) {
            return PrimitiveType.STRING;
        } else if (PrimitiveType.INT.getType().equals(typeString)) {
            return PrimitiveType.INT;
        } else if (PrimitiveType.LONG.getType().equals(typeString)) {
            return PrimitiveType.LONG;
        } else if (PrimitiveType.DOUBLE.getType().equals(typeString)) {
            return PrimitiveType.DOUBLE;
        } else if (typeString.startsWith("array<")) {
            // TODO refine the parser more
            typeString = typeString.substring(0, "array<".length());
            typeString = typeString.substring(0, typeString.length() - 1);
            return new ArrayType(parseType(typeString));
        } else {
            throw new IllegalArgumentException(""); // TODO
        }
    }

    protected Table table;
    protected List<Pair> pairs;

    public TableSchema(Table table) {
        this(table, null);
    }

    public TableSchema(Table table, List<String> pairsOfColsAndTypes) {
        super(table.getName());
        this.table = table;
        setPairsOfColsAndTypes(pairsOfColsAndTypes);
    }

    public void setPairsOfColsAndTypes(List<String> pairsOfColsAndTypes) {
        this.pairs = new ArrayList<Pair>();
        if (pairsOfColsAndTypes == null || pairsOfColsAndTypes.isEmpty()) {
            return;
        }

        int num = pairsOfColsAndTypes.size();
        for (int i = 0; i < num; i++) {
            String pairString = pairsOfColsAndTypes.get(i);
            this.pairs.add(parsePair(pairString));
        }
    }

    public Database getDatabase() {
        return table.getDatabase();
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return table.getName();
    }

    public List<Pair> getPairsOfColsAndTypes() {
        return pairs;
    }
}
