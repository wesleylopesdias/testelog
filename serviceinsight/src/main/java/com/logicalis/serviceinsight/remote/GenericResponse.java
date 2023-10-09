package com.logicalis.serviceinsight.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "correlationId",
    "message",
    "status"
})
public class GenericResponse {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("correlationId")
    private Long correlationId;
    @JsonProperty("message")
    private String message;
    @JsonProperty("status")
    private String status;

    public GenericResponse() {
		super();
    }

    public GenericResponse(Long id, Long correlationId, String status, String message) {
		super();
                this.id = id;
                this.correlationId = correlationId;
                this.status = status;
                this.message = message;
    }

	@JsonProperty("id")
    public Long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

	@JsonProperty("correlationId")
    public Long getCorrelationId() {
        return correlationId;
    }

    @JsonProperty("correlationId")
    public void setCorrelationId(Long correlationId) {
        this.correlationId = correlationId;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

	@Override
	public String toString() {
		return "GenericResponse [id=" + id + ", correlationId=" + correlationId + ", message=" + message
				+ ", status=" + status + "]";
	}

}
