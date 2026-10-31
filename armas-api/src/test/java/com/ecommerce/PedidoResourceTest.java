package com.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@io.quarkus.test.security.TestSecurity(user = "admin@teste.local", roles = "ADMIN")
class PedidoResourceTest {
    @Test
    void deveMostrarItensPorClienteEControlarEstoqueAoConfirmarECancelar() {
        long clienteId = given().contentType(ContentType.JSON).body("""
                {"nome":"Cliente Pedido","email":"pedido@teste.com","telefone":"11955555555",
                 "cpf":"11144477735","cep":"01001000","logradouro":"Praca da Se","numero":"100",
                 "bairro":"Se","municipioNome":"Sao Paulo","municipioCodigoIbge":"3550308",
                 "estadoSigla":"SP","estadoNome":"Sao Paulo"}
                """).when().post("/clientes").then().statusCode(201).extract().jsonPath().getLong("id");
        long categoriaId = given().contentType(ContentType.JSON).body("{\"nome\":\"Categoria Pedido\"}")
                .when().post("/categorias").then().statusCode(201).extract().jsonPath().getLong("id");
        long marcaId = given().contentType(ContentType.JSON).body("{\"nome\":\"Marca Pedido\"}")
                .when().post("/marcas").then().statusCode(201).extract().jsonPath().getLong("id");
        long modeloId = given().contentType(ContentType.JSON)
                .body("{\"nome\":\"Modelo Pedido\",\"marcaId\":" + marcaId + "}")
                .when().post("/modelos").then().statusCode(201).extract().jsonPath().getLong("id");
        long produtoId = given().contentType(ContentType.JSON).body("""
                {"nome":"Produto Pedido","preco":49.90,"estoque":3,"categoriaId":%d,"modeloId":%d}
                """.formatted(categoriaId, modeloId))
                .when().post("/produtos").then().statusCode(201)
                .body("fornecedorId", equalTo(null))
                .extract().jsonPath().getLong("id");

        long pedidoId = given().contentType(ContentType.JSON).body("""
                {"clienteId":%d,"itens":[{"produtoId":%d,"quantidade":2}]}
                """.formatted(clienteId, produtoId))
                .when().post("/pedidos").then().statusCode(201)
                .body("status", equalTo("PENDENTE"))
                .body("itens.nomeProduto", hasItem("Produto Pedido"))
                .body("total", equalTo(99.8f))
                .extract().jsonPath().getLong("id");

        given().queryParam("clienteId", clienteId).when().get("/pedidos")
                .then().statusCode(200).body("$", hasSize(1))
                .body("[0].id", equalTo((int) pedidoId));
        given().when().get("/produtos/{id}", produtoId).then().body("estoque", equalTo(3));
        given().when().delete("/clientes/{id}", clienteId).then().statusCode(409);
        given().when().delete("/produtos/{id}", produtoId).then().statusCode(409);

        given().contentType(ContentType.JSON).body("{\"status\":\"CONFIRMADO\"}")
                .when().put("/pedidos/{id}/status", pedidoId).then().statusCode(200)
                .body("status", equalTo("CONFIRMADO"));
        given().when().get("/produtos/{id}", produtoId).then().body("estoque", equalTo(1));

        long outroPedidoId = given().contentType(ContentType.JSON).body("""
                {"clienteId":%d,"itens":[{"produtoId":%d,"quantidade":2}]}
                """.formatted(clienteId, produtoId))
                .when().post("/pedidos").then().statusCode(201).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body("{\"status\":\"CONFIRMADO\"}")
                .when().put("/pedidos/{id}/status", outroPedidoId).then().statusCode(409);
        given().when().get("/produtos/{id}", produtoId).then().body("estoque", equalTo(1));

        given().contentType(ContentType.JSON).body("{\"status\":\"CANCELADO\"}")
                .when().put("/pedidos/{id}/status", pedidoId).then().statusCode(200)
                .body("status", equalTo("CANCELADO"));
        given().when().get("/produtos/{id}", produtoId).then().body("estoque", equalTo(3));
        given().contentType(ContentType.JSON).body("{\"status\":\"CONFIRMADO\"}")
                .when().put("/pedidos/{id}/status", pedidoId).then().statusCode(409);
    }

    @Test
    void deveEditarEExcluirCategoriaSemProduto() {
        long id = given().contentType(ContentType.JSON).body("{\"nome\":\"Categoria Editavel\"}")
                .when().post("/categorias").then().statusCode(201).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body("{\"nome\":\"Categoria Renomeada\"}")
                .when().put("/categorias/{id}", id).then().statusCode(200)
                .body("nome", equalTo("Categoria Renomeada"));
        given().when().delete("/categorias/{id}", id).then().statusCode(204);
    }
}
