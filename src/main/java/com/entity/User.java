package com.entity;



import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "m_user")
public class User {
    @Id
    private String username;
    private String password;
    private Boolean enabled;
    @Temporal(TemporalType.TIMESTAMP)
    private Date auditDate;

    @ManyToMany(fetch = FetchType.EAGER,mappedBy = "users",cascade = CascadeType.DETACH)
    private Set<Role> roles;

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }
}
