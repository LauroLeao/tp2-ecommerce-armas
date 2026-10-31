package com.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@QuarkusTest
@io.quarkus.test.security.TestSecurity(user = "admin@teste.local", roles = "ADMIN")
class ClienteResourceTest {

    @Test
    void deveSalvarPreservarETirarFotoOpcional() throws IOException {
        byte[] png = imagemPng();
        String dados = clienteJson("Cliente com foto", "foto@teste.com", "11977777777", "98765432100");
        Long id = given().contentType(ContentType.JSON)
                .body(comFoto(dados, "data:image/png;base64," + Base64.getEncoder().encodeToString(png)))
                .when().post("/clientes").then().statusCode(201)
                .body("fotoUrl", notNullValue())
                .extract().jsonPath().getLong("id");

        assertArrayEquals(png, given().when().get("/clientes/{id}/foto", id)
                .then().statusCode(200).contentType("image/png")
                .header("X-Content-Type-Options", "nosniff")
                .extract().asByteArray());

        given().contentType(ContentType.JSON).body(dados)
                .when().put("/clientes/{id}", id).then().statusCode(200)
                .body("fotoUrl", notNullValue());

        given().contentType(ContentType.JSON).body(dados.replace("}", ", \"removerFoto\": true}"))
                .when().put("/clientes/{id}", id).then().statusCode(200)
                .body("fotoUrl", nullValue());
        given().when().get("/clientes/{id}/foto", id).then().statusCode(404);
        given().when().delete("/clientes/{id}", id).then().statusCode(204);
    }

    @Test
    void deveRejeitarFotoInvalidaSemCriarCliente() {
        String dados = clienteJson("Cliente sem foto valida", "foto-invalida@teste.com", "11988888888", "98765432100");
        given().contentType(ContentType.JSON)
                .body(comFoto(dados, "data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[] {1, 2, 3})))
                .when().post("/clientes").then().statusCode(400)
                .body("mensagem", notNullValue());
    }

    private String comFoto(String dados, String foto) {
        return dados.replace("}", ", \"fotoBase64\": \"" + foto + "\"}");
    }

    private byte[] imagemPng() throws IOException {
        BufferedImage imagem = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            ImageIO.write(imagem, "png", saida);
            return saida.toByteArray();
        }
    }

    @Test
    void deveExecutarCrudCompleto() {
        Long id = given()
                .contentType(ContentType.JSON)
                .body(clienteJson("Cliente de teste", "cliente@teste.com", "63999999999", "12345678909"))
                .when().post("/clientes")
                .then()
                .statusCode(201)
                .body("nome", equalTo("Cliente de teste"))
                .body("municipioNome", equalTo("Sao Paulo"))
                .body("estadoSigla", equalTo("SP"))
                .extract().jsonPath().getLong("id");

        given()
                .when().get("/clientes")
                .then()
                .statusCode(200)
                .body("email", hasItem("cliente@teste.com"));

        given()
                .queryParam("nome", "DE TESTE")
                .when().get("/clientes/sugestoes")
                .then()
                .statusCode(200)
                .body("nome", hasItem("Cliente de teste"));

        given()
                .queryParam("nome", "x")
                .when().get("/clientes/sugestoes")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));

        given()
                .contentType(ContentType.JSON)
                .body(clienteJson("Cliente atualizado", "atualizado@teste.com", "63988888888", "12345678909"))
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
                        {"nome":"", "email":"invalido", "telefone":"", "cpf":"", "cep":""}
                        """)
                .when().post("/clientes")
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("email"));
    }

    @Test
    void devePermitirEnderecoIgualEImpedirCpfEmailETelefoneDuplicados() {
        Long primeiroId = given().contentType(ContentType.JSON)
                .body(clienteJson("Primeiro cliente", "primeiro@teste.com", "11911111111", "98765432100"))
                .when().post("/clientes").then().statusCode(201)
                .extract().jsonPath().getLong("id");

        Long segundoId = given().contentType(ContentType.JSON)
                .body(clienteJson("Segundo cliente", "segundo@teste.com", "11922222222", "12312312387"))
                .when().post("/clientes").then().statusCode(201)
                .body("endereco", equalTo("Praca da Se, 100 - Sao Paulo/SP"))
                .extract().jsonPath().getLong("id");

        given().contentType(ContentType.JSON)
                .body(clienteJson("Outro cliente", "outro@teste.com", "11933333333", "98765432100"))
                .when().post("/clientes").then().statusCode(409)
                .body("field", equalTo("cpf"));

        given().contentType(ContentType.JSON)
                .body(clienteJson("Outro cliente", "primeiro@teste.com", "11933333333", "32132132178"))
                .when().post("/clientes").then().statusCode(409)
                .body("field", equalTo("email"));

        given().contentType(ContentType.JSON)
                .body(clienteJson("Outro cliente", "outro@teste.com", "11911111111", "32132132178"))
                .when().post("/clientes").then().statusCode(409)
                .body("field", equalTo("telefone"));

        given().when().delete("/clientes/{id}", primeiroId).then().statusCode(204);
        given().when().delete("/clientes/{id}", segundoId).then().statusCode(204);
    }

    @Test
    void deveRejeitarCpfComDigitosVerificadoresInvalidos() {
        given().contentType(ContentType.JSON)
                .body(clienteJson("Cliente invalido", "cpf-invalido@teste.com", "11944444444", "12345678900"))
                .when().post("/clientes").then().statusCode(400)
                .body("field", equalTo("cpf"));
    }

    @Test
    void devePermitirEnderecoEstruturadoSemCepEValidarCepQuandoInformado() {
        String semCep = clienteJson("Cliente sem CEP", "sem-cep@teste.com", "11955555555", "12345678909")
                .replace("\"cep\": \"01001000\"", "\"cep\": null");

        Long id = given().contentType(ContentType.JSON)
                .body(semCep)
                .when().post("/clientes")
                .then().statusCode(201)
                .body("cep", nullValue())
                .body("municipioCodigoIbge", equalTo("3550308"))
                .body("estadoSigla", equalTo("SP"))
                .extract().jsonPath().getLong("id");

        given().contentType(ContentType.JSON)
                .body(clienteJson("Cliente com CEP curto", "cep-curto@teste.com", "11966666666", "12312312387")
                        .replace("\"cep\": \"01001000\"", "\"cep\": \"123\""))
                .when().post("/clientes")
                .then().statusCode(400)
                .body("errors.field", hasItem("cep"));

        given().when().delete("/clientes/{id}", id).then().statusCode(204);
    }

    private String clienteJson(String nome, String email, String telefone, String cpf) {
        return """
                {
                  "nome": "%s", "email": "%s", "telefone": "%s", "cpf": "%s",
                  "cep": "01001000", "logradouro": "Praca da Se", "numero": "100",
                  "complemento": "", "bairro": "Se", "municipioNome": "Sao Paulo",
                  "municipioCodigoIbge": "3550308", "estadoSigla": "SP", "estadoNome": "Sao Paulo"
                }
                """.formatted(nome, email, telefone, cpf);
    }
}
