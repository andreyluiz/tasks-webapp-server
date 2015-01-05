package br.com.tasks.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.lang.Override;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "`task`")
@XmlRootElement
public class Task implements Serializable {

	private static final long serialVersionUID = 8867477050973525727L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column
	private String description;

	@Column
	private Boolean done = false;

	@Version
	@Column(name = "version")
	private int version;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date creation;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date conclusion;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public Date getConclusion() {
		return conclusion;
	}

	public void setConclusion(Date conclusion) {
		this.conclusion = conclusion;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		if (description != null && !description.trim().isEmpty())
			result += ", description: " + description;
		if (done != null)
			result += ", done: " + done;
		result += ", version: " + version;
		if (creation != null)
			result += ", creation: " + creation;
		if (conclusion != null)
			result += ", conclusion: " + conclusion;
		return result;
	}
}