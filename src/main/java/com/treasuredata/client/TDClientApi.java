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
package com.treasuredata.client;

import com.google.common.base.Function;
import com.treasuredata.client.model.TDBulkImportSession;
import com.treasuredata.client.model.TDBulkLoadSessionStartRequest;
import com.treasuredata.client.model.TDBulkLoadSessionStartResult;
import com.treasuredata.client.model.TDColumn;
import com.treasuredata.client.model.TDDatabase;
import com.treasuredata.client.model.TDExportJobRequest;
import com.treasuredata.client.model.TDJob;
import com.treasuredata.client.model.TDJobList;
import com.treasuredata.client.model.TDJobRequest;
import com.treasuredata.client.model.TDJobSummary;
import com.treasuredata.client.model.TDPartialDeleteJob;
import com.treasuredata.client.model.TDResultFormat;
import com.treasuredata.client.model.TDSaveQueryRequest;
import com.treasuredata.client.model.TDSavedQuery;
import com.treasuredata.client.model.TDSavedQueryUpdateRequest;
import com.treasuredata.client.model.TDTable;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Treasure Data Client API
 */
public interface TDClientApi<ClientImpl>
        extends AutoCloseable
{
    /**
     * Return a TDClientApi implementation that uses the given api key.
     * This instance will share the same internal http client, so closing the returned client will invalidate the current instance.
     *
     * @param newApiKey
     * @return
     */
    ClientImpl withApiKey(String newApiKey);

    /**
     * Perform user email and password based authentication and return a new client that will use apikey based authentication.
     * Similary to {@link #withApiKey(String)} method, closing the returned client will invalidate the current instance.
     *
     * @param email
     * @param password
     * @return
     */
    ClientImpl authenticate(String email, String password);

    String serverStatus();

    // Database operations

    /**
     * Get the list of databases
     *
     * @return list of databases
     * @throws TDClientException if failed to retrieve the database list
     */
    List<String> listDatabaseNames();

    /**
     * Get the detailed information of databases
     *
     * @return list of TDDatabase
     * @throws TDClientException if failed to retrieve the database list.
     */
    List<TDDatabase> listDatabases();

    /**
     * Create a new database
     *
     * @param databaseName
     * @throws TDClientException if the specified database already exists
     */
    void createDatabase(String databaseName);

    void createDatabaseIfNotExists(String databaseName);

    /**
     * Delete a specified database. Deleting a database deletes all of its belonging tables.
     *
     * @param databaseName
     * @throws TDClientException if no such a database exists
     */
    void deleteDatabase(String databaseName);

    void deleteDatabaseIfExists(String databaseName);

    // Table operations

    /**
     * Get the list of the tables in the specified database
     *
     * @param databaseName
     * @return
     * @throws TDClientException
     */
    List<TDTable> listTables(String databaseName);

    boolean existsDatabase(String databaseName);

    boolean existsTable(String databaseName, String table);

    /**
     * Create a new table
     *
     * @param databaseName
     * @param tableName
     * @return
     * @throws TDClientException
     */
    void createTable(String databaseName, String tableName);

    void createTableIfNotExists(String databaseName, String tableName);

    void renameTable(String databaseName, String tableName, String newTableName);

    void renameTable(String databaseName, String tableName, String newTableName, boolean overwrite);

    void deleteTable(String databaseName, String tableName);

    void deleteTableIfExists(String databaseName, String tableName);

    TDPartialDeleteJob partialDelete(String databaseName, String tableName, long from, long to);

    void swapTables(String databaseName, String tableName1, String tableName2);

    // schema API
    void updateTableSchema(String databaseName, String tableName, List<TDColumn> newSchema);

    /**
     * Submit a new job request
     *
     * @param jobRequest
     * @return job_id
     * @throws TDClientException
     */
    String submit(TDJobRequest jobRequest);

    TDJobList listJobs();

    TDJobList listJobs(long fromJobId, long toJobId);

    void killJob(String jobId);

    TDJobSummary jobStatus(String jobId);

    TDJob jobInfo(String jobId);

    /**
     * Open an input stream to retrieve the job result.
     * The input stream will be closed after this method
     * <p/>
     * You will receive an empty stream if the query has not finished yet.
     *
     * @param jobId
     * @param format
     * @param resultStreamHandler
     * @return
     */
    <Result> Result jobResult(String jobId, TDResultFormat format, Function<InputStream, Result> resultStreamHandler);

    // bulk import API
    List<TDBulkImportSession> listBulkImportSessions();

    List<String> listBulkImportParts(String sessionName);

    void createBulkImportSession(String sessionName, String databaseName, String tableName);

    TDBulkImportSession getBulkImportSession(String sessionName);

    void uploadBulkImportPart(String sessionName, String uniquePartName, File path);

    void freezeBulkImportSession(String sessionName);

    void unfreezeBulkImportSession(String sessionName);

    void performBulkImportSession(String sessionName);

    void performBulkImportSession(String sessionName, TDJob.Priority priority);

    void commitBulkImportSession(String sessionName);

    void deleteBulkImportSession(String sessionName);

    <Result> Result getBulkImportErrorRecords(String sessionName, Function<InputStream, Result> resultStreamHandler);

    /**
     * Saved query APIs
     */

    /**
     * Start a query saved on the cloud.
     *
     * @param name name of the saved query
     * @param scheduledTime the return time of TD_SCHEDULED_TIME
     * @return job id
     */
    String startSavedQuery(String name, Date scheduledTime);

    List<TDSavedQuery> listSavedQueries();

    /**
     * Save a query for scheduling. Use {@link TDSavedQuery#newBuilder(String, TDJob.Type, String, String, String)}
     * to create a TDSaveQueryRequest.
     *
     * @param request
     * @return
     */
    TDSavedQuery saveQuery(TDSaveQueryRequest request);

    /**
     * Update the saved query of the given name. To build an update request, use {@link TDSavedQuery#newUpdateRequestBuilder()}.
     *
     * @param name
     * @param request
     * @return
     */
    TDSavedQuery updateSavedQuery(String name, TDSavedQueryUpdateRequest request);

    /**
     * Delete the saved query of the given name.
     *
     * @param name
     * @return
     */
    TDSavedQuery deleteSavedQuery(String name);

    /**
     * Start a table export job.
     *
     * @param jobRequest
     * @return job id
     */
    String submitExportJob(TDExportJobRequest jobRequest);

    /*
     * Data Connector Bulk Loading Session APIs
     */

    /**
     * Start a Data Connector Bulk Loading Session Job.
     * @param name The name of the Data Connector Bulk Loading Session.
     * @return job id
     */
    TDBulkLoadSessionStartResult startBulkLoadSession(String name);

    /**
     * Start a Data Connector Bulk Loading Session Job.
     * @param name The name of the Data Connector Bulk Loading Session.
     * @param scheduledTime The unix epoch to use as the scheduled time of the job.
     * @return job id
     */
    TDBulkLoadSessionStartResult startBulkLoadSession(String name, long scheduledTime);

    /**
     * Start a Data Connector Bulk Loading Session Job.
     * @param request
     * @return job id
     */
    TDBulkLoadSessionStartResult startBulkLoadSession(String name, TDBulkLoadSessionStartRequest request);
}
