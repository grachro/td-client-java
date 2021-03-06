package com.treasuredata.client.model;

import com.google.common.base.Optional;
import org.junit.Test;

import static com.treasuredata.client.model.ObjectMappers.compactMapper;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TDBulkLoadSessionStartRequestTest
{
    @Test
    public void defaultValues()
            throws Exception
    {
        assertThat(TDBulkLoadSessionStartRequest.builder().build().getScheduledTime(), is(Optional.<Long>absent()));
    }

    @Test
    public void testEquals()
            throws Exception
    {
        assertThat(TDBulkLoadSessionStartRequest.of(), is(TDBulkLoadSessionStartRequest.builder().build()));

        assertThat(TDBulkLoadSessionStartRequest.of(), is(not(TDBulkLoadSessionStartRequest.builder()
                .setScheduledTime(17L)
                .build())));

        assertThat(TDBulkLoadSessionStartRequest.builder()
                        .setScheduledTime(17L)
                        .build(),
                is(TDBulkLoadSessionStartRequest.builder()
                        .setScheduledTime(17L)
                        .build()));

        assertThat(TDBulkLoadSessionStartRequest.builder()
                        .setScheduledTime(18L)
                        .build(),
                is(not(TDBulkLoadSessionStartRequest.builder()
                        .setScheduledTime(17L)
                        .build())));
    }

    @Test
    public void testCompactMapper()
            throws Exception
    {
        assertThat(compactMapper().writeValueAsString(TDBulkLoadSessionStartRequest.of()), is("{}"));
        assertThat(compactMapper().writeValueAsString(TDBulkLoadSessionStartRequest.builder().setScheduledTime(17L).build()), is("{\"scheduled_time\":17}"));
    }
}
