package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "address")
public class Address {

    @Id
    @UuidGenerator
    @Column(name = "address_id")
    private UUID addressId;

    private String country;

    private String city;

    private String street;

    private String house;

    private String flat;
}
