package com.function.execution.component.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Table(name ="content")
@TypeDef(name = "jsonb", typeClass = com.vladmihalcea.hibernate.type.json.JsonBinaryType.class)
@Setter
@Getter
@Entity
public class ExecutionEntity {

    @Id
    private String id;

    private Timestamp createdOn;

    private Timestamp updatedOn;

    private String schema;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private JsonNode data;

}
