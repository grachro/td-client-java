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

import com.google.common.base.Optional;

import java.util.Properties;

import static com.treasuredata.client.TDClientConfig.ENV_TD_CLIENT_APIKEY;
import static com.treasuredata.client.TDClientConfig.Type.APIKEY;
import static com.treasuredata.client.TDClientConfig.Type.API_ENDPOINT;
import static com.treasuredata.client.TDClientConfig.Type.API_PORT;
import static com.treasuredata.client.TDClientConfig.Type.CONNECTION_POOL_SIZE;
import static com.treasuredata.client.TDClientConfig.Type.CONNECT_TIMEOUT_MILLIS;
import static com.treasuredata.client.TDClientConfig.Type.IDLE_TIMEOUT_MILLIS;
import static com.treasuredata.client.TDClientConfig.Type.PASSOWRD;
import static com.treasuredata.client.TDClientConfig.Type.PROXY_HOST;
import static com.treasuredata.client.TDClientConfig.Type.PROXY_PASSWORD;
import static com.treasuredata.client.TDClientConfig.Type.PROXY_PORT;
import static com.treasuredata.client.TDClientConfig.Type.PROXY_USER;
import static com.treasuredata.client.TDClientConfig.Type.PROXY_USESSL;
import static com.treasuredata.client.TDClientConfig.Type.RETRY_INITIAL_INTERVAL_MILLIS;
import static com.treasuredata.client.TDClientConfig.Type.RETRY_LIMIT;
import static com.treasuredata.client.TDClientConfig.Type.RETRY_MAX_INTERVAL_MILLIS;
import static com.treasuredata.client.TDClientConfig.Type.RETRY_MULTIPLIER;
import static com.treasuredata.client.TDClientConfig.Type.USER;
import static com.treasuredata.client.TDClientConfig.Type.USESSL;
import static com.treasuredata.client.TDClientConfig.getTDConfProperties;

/**
 *
 */
public abstract class AbstractTDClientBuilder<ClientImpl>
{
    protected Optional<String> endpoint = Optional.absent();
    protected Optional<Integer> port = Optional.absent();
    protected boolean useSSL = true;
    protected Optional<String> apiKey = Optional.absent();
    protected Optional<String> user = Optional.absent();
    protected Optional<String> password = Optional.absent();
    protected Optional<ProxyConfig> proxy = Optional.absent();
    protected int retryLimit = 7;
    protected int retryInitialIntervalMillis = 500;
    protected int retryMaxIntervalMillis = 60000;
    protected double retryMultiplier = 2.0;
    protected int connectTimeoutMillis = 15000;
    protected int idleTimeoutMillis = 60000;
    protected int connectionPoolSize = 64;

    private static Optional<String> getConfigProperty(Properties p, TDClientConfig.Type key)
    {
        return getConfigProperty(p, key.key);
    }

    private static Optional<String> getConfigProperty(Properties p, String key)
    {
        return Optional.fromNullable(p.getProperty(key));
    }

    private static Optional<Integer> getConfigPropertyInt(Properties p, TDClientConfig.Type key)
    {
        String v = p.getProperty(key.key);
        if (v != null) {
            try {
                return Optional.of(Integer.parseInt(v));
            }
            catch (NumberFormatException e) {
                throw new TDClientException(TDClientException.ErrorType.INVALID_CONFIGURATION, String.format("[%s] cannot cast %s to integer", key, v));
            }
        }
        else {
            return Optional.absent();
        }
    }

    private static Optional<Double> getConfigPropertyDouble(Properties p, TDClientConfig.Type key)
    {
        String v = p.getProperty(key.key);
        if (v != null) {
            try {
                return Optional.of(Double.parseDouble(v));
            }
            catch (NumberFormatException e) {
                throw new TDClientException(TDClientException.ErrorType.INVALID_CONFIGURATION, String.format("[%s] cannot cast %s to double", key, v));
            }
        }
        else {
            return Optional.absent();
        }
    }

    /**
     * Create a new TDClinenb builder whose configuration is initialized with System Properties and $HOME/.td/td.conf values.
     * Precedence of properties is the following order:
     * <ol>
     * <li>System Properties</li>
     * <li>$HOME/.td/td.conf values</li>
     * </ol>
     *
     * @return
     */
    protected AbstractTDClientBuilder(boolean loadTDConf)
    {
        // load the environnment variable for the api key
        String apiKeyEnv = System.getenv(ENV_TD_CLIENT_APIKEY);
        if (apiKeyEnv != null) {
            setApiKey(apiKeyEnv);
        }

        // load system properties
        setProperties(System.getProperties());

        // We also read $HOME/.td/td.conf file for apikey, user, and password values
        if (loadTDConf) {
            setProperties(getTDConfProperties());
        }
    }

    /**
     * Override the TDClient configuration with the given Properties
     *
     * @param p
     * @return
     */
    public AbstractTDClientBuilder<ClientImpl> setProperties(Properties p)
    {
        this.endpoint = getConfigProperty(p, API_ENDPOINT).or(endpoint);
        this.port = getConfigPropertyInt(p, API_PORT).or(port);
        if (p.containsKey(USESSL.key)) {
            setUseSSL(Boolean.parseBoolean(p.getProperty(USESSL.key)));
        }
        this.apiKey = getConfigProperty(p, APIKEY)
                .or(getConfigProperty(p, "apikey"))
                .or(apiKey);
        this.user = getConfigProperty(p, USER)
                .or(getConfigProperty(p, "user"))
                .or(user);
        this.password = getConfigProperty(p, PASSOWRD)
                .or(getConfigProperty(p, "password"))
                .or(password);

        // proxy
        boolean hasProxy = false;
        ProxyConfig.ProxyConfigBuilder proxyConfig;
        if (proxy.isPresent()) {
            hasProxy = true;
            proxyConfig = new ProxyConfig.ProxyConfigBuilder(proxy.get());
        }
        else {
            proxyConfig = new ProxyConfig.ProxyConfigBuilder();
        }
        Optional<String> proxyHost = getConfigProperty(p, PROXY_HOST);
        Optional<Integer> proxyPort = getConfigPropertyInt(p, PROXY_PORT);
        Optional<String> proxyUseSSL = getConfigProperty(p, PROXY_USESSL);
        Optional<String> proxyUser = getConfigProperty(p, PROXY_USER);
        Optional<String> proxyPassword = getConfigProperty(p, PROXY_PASSWORD);
        if (proxyHost.isPresent()) {
            hasProxy = true;
            proxyConfig.setHost(proxyHost.get());
        }
        if (proxyPort.isPresent()) {
            hasProxy = true;
            proxyConfig.setPort(proxyPort.get());
        }
        if (proxyUseSSL.isPresent()) {
            hasProxy = true;
            proxyConfig.useSSL(Boolean.parseBoolean(proxyUseSSL.get()));
        }
        if (proxyUser.isPresent()) {
            hasProxy = true;
            proxyConfig.setUser(proxyUser.get());
        }
        if (proxyPassword.isPresent()) {
            hasProxy = true;
            proxyConfig.setPassword(proxyPassword.get());
        }
        this.proxy = Optional.fromNullable(hasProxy ? proxyConfig.createProxyConfig() : null);

        // http client parameter
        this.retryLimit = getConfigPropertyInt(p, RETRY_LIMIT).or(retryLimit);
        this.retryInitialIntervalMillis = getConfigPropertyInt(p, RETRY_INITIAL_INTERVAL_MILLIS).or(retryInitialIntervalMillis);
        this.retryMaxIntervalMillis = getConfigPropertyInt(p, RETRY_MAX_INTERVAL_MILLIS).or(retryMaxIntervalMillis);
        this.retryMultiplier = getConfigPropertyDouble(p, RETRY_MULTIPLIER).or(retryMultiplier);
        this.connectTimeoutMillis = getConfigPropertyInt(p, CONNECT_TIMEOUT_MILLIS).or(connectTimeoutMillis);
        this.idleTimeoutMillis = getConfigPropertyInt(p, IDLE_TIMEOUT_MILLIS).or(idleTimeoutMillis);
        this.connectionPoolSize = getConfigPropertyInt(p, CONNECTION_POOL_SIZE).or(connectionPoolSize);

        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setEndpoint(String endpoint)
    {
        this.endpoint = Optional.of(endpoint);
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setPort(int port)
    {
        this.port = Optional.of(port);
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setUseSSL(boolean useSSL)
    {
        this.useSSL = useSSL;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setApiKey(String apiKey)
    {
        this.apiKey = Optional.of(apiKey);
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setUser(String user)
    {
        this.user = Optional.of(user);
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setPassword(String password)
    {
        this.password = Optional.of(password);
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setProxy(ProxyConfig proxyConfig)
    {
        this.proxy = Optional.of(proxyConfig);
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setRetryLimit(int retryLimit)
    {
        this.retryLimit = retryLimit;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setRetryInitialIntervalMillis(int retryInitialIntervalMillis)
    {
        this.retryInitialIntervalMillis = retryInitialIntervalMillis;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setRetryMaxIntervalMillis(int retryMaxIntervalMillis)
    {
        this.retryMaxIntervalMillis = retryMaxIntervalMillis;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setRetryMultiplier(double retryMultiplier)
    {
        this.retryMultiplier = retryMultiplier;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setConnectTimeoutMillis(int connectTimeoutMillis)
    {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setIdleTimeoutMillis(int idleTimeoutMillis)
    {
        this.idleTimeoutMillis = idleTimeoutMillis;
        return this;
    }

    public AbstractTDClientBuilder<ClientImpl> setConnectionPoolSize(int connectionPoolSize)
    {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }

    /**
     * Build a config object.
     * @return
     */
    public TDClientConfig buildConfig()
    {
        return new TDClientConfig(
                endpoint,
                port,
                useSSL,
                apiKey,
                user,
                password,
                proxy,
                retryLimit,
                retryInitialIntervalMillis,
                retryMaxIntervalMillis,
                retryMultiplier,
                connectTimeoutMillis,
                idleTimeoutMillis,
                connectionPoolSize
        );
    }

    public abstract ClientImpl build();
}
