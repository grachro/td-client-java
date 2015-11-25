/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.treasuredata.client.model;

import com.google.common.base.Optional;

/**
 *
 */
public class TDJobRequest
{
    private final String database;
    private final TDJob.Type type;
    private final String query;
    private final TDJob.Priority priority;
    private final Optional<String> resultOutput;
    private final int retryLimit;
    private final Optional<String> poolName;

    public TDJobRequest(String database, TDJob.Type type, String query, TDJob.Priority priority, Optional<String> resultOutput, int retryLimit, Optional<String> poolName)
    {
        this.database = database;
        this.type = type;
        this.query = query;
        this.priority = priority;
        this.resultOutput = resultOutput;
        this.retryLimit = retryLimit;
        this.poolName = poolName;
    }

    public static TDJobRequest newPrestoQuery(String database, String query)
    {
        return new TDJobRequestBuilder()
                .setType(TDJob.Type.PRESTO)
                .setDatabase(database)
                .setQuery(query)
                .createTDJobRequest();
    }

    public static TDJobRequest newPrestoQuery(String database, String query, String resultOutput)
    {
        return new TDJobRequestBuilder()
                .setType(TDJob.Type.PRESTO)
                .setDatabase(database)
                .setQuery(query)
                .setResultOutput(resultOutput)
                .createTDJobRequest();
    }

    public static TDJobRequest newHiveQuery(String database, String query)
    {
        return new TDJobRequestBuilder().setType(TDJob.Type.HIVE)
                .setDatabase(database)
                .setQuery(query)
                .createTDJobRequest();
    }

    public static TDJobRequest newHiveQuery(String database, String query, String resultOutput)
    {
        return new TDJobRequestBuilder().setType(TDJob.Type.HIVE)
                .setDatabase(database)
                .setQuery(query)
                .setResultOutput(resultOutput)
                .createTDJobRequest();
    }

    public static TDJobRequest newHiveQuery(String database, String query, String resultOutput, String poolName)
    {
        return new TDJobRequestBuilder().setType(TDJob.Type.HIVE)
                .setDatabase(database)
                .setQuery(query)
                .setResultOutput(resultOutput)
                .setPoolName(poolName)
                .createTDJobRequest();
    }

    public String getDatabase()
    {
        return database;
    }

    public TDJob.Type getType()
    {
        return type;
    }

    public String getQuery()
    {
        return query;
    }

    public TDJob.Priority getPriority()
    {
        return priority;
    }

    public int getRetryLimit()
    {
        return retryLimit;
    }

    public Optional<String> getResultOutput()
    {
        return resultOutput;
    }

    public Optional<String> getPoolName()
    {
        return poolName;
    }

    @Override
    public String toString()
    {
        return "TDJobRequest{" +
                "database='" + database + '\'' +
                ", type=" + type +
                ", query='" + query + '\'' +
                ", priority=" + priority +
                ", resultOutput=" + resultOutput +
                ", retryLimit=" + retryLimit +
                '}';
    }
}
