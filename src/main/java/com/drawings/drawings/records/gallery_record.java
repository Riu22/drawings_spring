package com.drawings.drawings.records;


import java.sql.Timestamp;

public record gallery_record(
        int id,
        String title,
        Timestamp createdAt,
        boolean isPublic,
        int latestVersionNumber,
        String drawContent
) {

}