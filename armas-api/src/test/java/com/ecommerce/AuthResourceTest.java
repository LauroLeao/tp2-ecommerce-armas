package com.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class AuthResourceTest {
    @Test
    void deveCriarContaDeClienteEImpedirAcessoAdministrativo() {
        given().when().get("/produtos").then().statusCode(401);

        String cadastro = """
                {"cliente":{"nome":"Cliente da Conta","email":"conta@teste.local","telefone":"11998887766",
                 "cpf":"52998224725","cep":"01001000","logradouro":"Praca da Se","numero":"10",
                 "bairro":"Se","municipioNome":"Sao Paulo","municipioCodigoIbge":"3550308",
                 "estadoSigla":"SP","estadoNome":"Sao Paulo"},"senha":"SenhaForte123"}
                """;
        given().contentType(ContentType.JSON).body(cadastro).when().post("/auth/cadastro")
                .then().statusCode(201).body("email", equalTo("conta@teste.local"));
        given().contentType(ContentType.JSON).body(cadastro).when().post("/auth/cadastro")
                .then().statusCode(409);

        String cookie = given().formParam("j_username", "conta@teste.local")
                .formParam("j_password", "SenhaForte123")
                .when().post("/j_security_check")
                .then().statusCode(200).extract().cookie("quarkus-credential");

        given().cookie("quarkus-credential", cookie).when().get("/auth/me")
                .then().statusCode(200).body("perfil", equalTo("CLIENTE"));
        given().cookie("quarkus-credential", cookie).when().get("/painel/cliente")
                .then().statusCode(200).body("pedidos", equalTo(0));
        given().cookie("quarkus-credential", cookie).when().get("/produtos")
                .then().statusCode(403);
        given().cookie("quarkus-credential", cookie).when().get("/painel/admin")
                .then().statusCode(403);
    }
}
