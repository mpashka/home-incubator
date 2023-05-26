package org.mpashka.spring.data.cassandra;

import java.time.Instant;
import java.util.Date;

import org.springframework.data.convert.Jsr310Converters;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.data.DefaultUdtValue;
import com.datastax.oss.driver.internal.core.type.UserDefinedTypeBuilder;

public class AttachmentDataItemCodec extends MappingCodec<UdtValue,Attachment.AttachmentData> {

    public AttachmentDataItemCodec() {
        super(TypeCodecs.udtOf(
//                                    properties.getKeyspaceName()
                        new UserDefinedTypeBuilder("geps", Attachment.AttachmentData.TYPE_NAME)
                                .withField(Attachment.AttachmentData.DT_FILE_NAME, DataTypes.TEXT)
                                .withField(Attachment.AttachmentData.DT_FILE_SIZE, DataTypes.BIGINT)
                                .withField(Attachment.AttachmentData.DT_MIME_TYPE, DataTypes.TEXT)
                                .withField(Attachment.AttachmentData.DT_SPF_BLANK_ID, DataTypes.TEXT)
                                .withField(Attachment.AttachmentData.DT_STATUS, DataTypes.ASCII)
                                .withField(Attachment.AttachmentData.DT_EXTERNAL_LINK, DataTypes.TEXT)
                                .withField(Attachment.AttachmentData.DT_CREATE_DATE, DataTypes.TIMESTAMP)
                                .withField(Attachment.AttachmentData.DT_UPDATE_DATE, DataTypes.TIMESTAMP)
                                .withField(Attachment.AttachmentData.DT_BASE_DIR_ID, DataTypes.SMALLINT)
                                .build()),
                GenericType.of(Attachment.AttachmentData.class));
    }

    @Override
    protected UdtValue outerToInner(final Attachment.AttachmentData value) {
        return new DefaultUdtValue((UserDefinedType) innerCodec.getCqlType(),
                value.getFileName(),
                value.getFileSize(),
                value.getMimeType(),
                value.getSpfBlankId(),
                value.getStatus(),
                value.getExternalLink(),
                value.getBaseDirId(),
                convertToInstant(value.getCreateDate()),
                convertToInstant(value.getUpdateDate()));
    }

    @Override
    protected Attachment.AttachmentData innerToOuter(final UdtValue value) {
        return Attachment.AttachmentData.builder()
                .fileName(value.getString(Attachment.AttachmentData.DT_FILE_NAME))
                .fileSize(value.getLong(Attachment.AttachmentData.DT_FILE_SIZE))
                .mimeType(value.getString(Attachment.AttachmentData.DT_MIME_TYPE))
                .spfBlankId(value.getString(Attachment.AttachmentData.DT_SPF_BLANK_ID))
                .status(value.getString(Attachment.AttachmentData.DT_STATUS))
                .externalLink(value.getString(Attachment.AttachmentData.DT_EXTERNAL_LINK))
                .baseDirId(value.getShort(Attachment.AttachmentData.DT_BASE_DIR_ID))
                .createDate(convertToDate(value.get(Attachment.AttachmentData.DT_CREATE_DATE, TypeCodecs.TIMESTAMP)))
                .updateDate(convertToDate(value.get(Attachment.AttachmentData.DT_UPDATE_DATE, TypeCodecs.TIMESTAMP)))
                .build();
    }

    private @Nullable Date convertToDate(final Instant instant) {
        if (instant == null) {
            return null;
        }
        return Jsr310Converters.InstantToDateConverter.INSTANCE.convert(instant);
    }

    private @Nullable Instant convertToInstant(final Date date) {
        if (date == null) {
            return null;
        }
        return Jsr310Converters.DateToInstantConverter.INSTANCE.convert(date);
    }
}
