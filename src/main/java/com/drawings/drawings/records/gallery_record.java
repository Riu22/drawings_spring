package com.drawings.drawings.records;

import java.sql.Timestamp;

public record gallery_record(
        int id,
        String title,
        Timestamp created_at,
        boolean is_public,
        int latest_version_number,
        String draw_content,
        boolean can_edit,
        int user_id
) {
}