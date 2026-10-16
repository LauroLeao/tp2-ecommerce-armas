package com.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class ClienteResourceTest {

    @Test
    void deveExecutarCrudCompleto() {
        Long id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Cliente de teste",
                          "email": "cliente@teste.com",
                          "telefone": "63999999999",
                          "endereco": "Endereco de teste"
                        }
                        """)
                .when().post("/clientes")
                .then()
                .statusCode(201)
                .body("nome", equalTo("Cliente de teste"))
                .extract().jsonPath().getLong("id");

        given()
                .when().get("/clientes")
                .then()
                .statusCode(200)
                .body("email", hasItem("cliente@teste.com"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Cliente atualizado",
                          "email": "atualizado@teste.com",
                          "telefone": "63988888888",
                          "endereco": "Novo endereco"
                        }
                        """)
                .when().put("/clientes/{id}", id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Cliente atualizado"));

        given()
                .when().delete("/clientes/{id}", id)
                .then()
                .statusCode(204);

        given()
                .when().get("/clientes/{id}", id)
                .then()
                .statusCode(404)
                .body("mensagem", equalTo("Cliente nao encontrado"));
    }

    @Test
    void deveRejeitarClienteInvalido() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"", "email":"invalido", "telefone":"", "endereco":""}
                        """)
                .when().post("/clientes")
                .then()
                .statusCode(400);
    }
}
