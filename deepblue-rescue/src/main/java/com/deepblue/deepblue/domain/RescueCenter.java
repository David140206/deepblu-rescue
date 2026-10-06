package com.deepblue.deepblue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rescue_centers")
public class RescueCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @OneToMany(mappedBy = "rescueCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RescueCase> rescueCases = new ArrayList<>();

    protected RescueCenter() {
    }

    public RescueCenter(String code, String name, String city) {
        this.code = code;
        this.name = name;
        this.city = city;
    }

    public void addCase(RescueCase rescueCase) {
        rescueCases.add(rescueCase);
        rescueCase.setRescueCenter(this);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public List<RescueCase> getRescueCases() { return rescueCases; }
}
