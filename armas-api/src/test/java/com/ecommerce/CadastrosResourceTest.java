package com.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@io.quarkus.test.security.TestSecurity(user = "admin@teste.local", roles = "ADMIN")
class CadastrosResourceTest {

    @Test
    void deveExecutarOsTresCrudsEFiltrarVinculosDoProduto() {
        long marcaId = given().contentType(ContentType.JSON).body(Map.of("nome", "Marca Relacionamento"))
                .when().post("/marcas").then().statusCode(201).extract().jsonPath().getLong("id");
        long outraMarcaId = given().contentType(ContentType.JSON).body(Map.of("nome", "Outra Marca Relacionamento"))
                .when().post("/marcas").then().statusCode(201).extract().jsonPath().getLong("id");
        long modeloId = given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Modelo Um", "marcaId", marcaId))
                .when().post("/modelos").then().statusCode(201).extract().jsonPath().getLong("id");
        long outroModeloId = given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Modelo Dois", "marcaId", outraMarcaId))
                .when().post("/modelos").then().statusCode(201).extract().jsonPath().getLong("id");
        long fornecedorId = given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Distribuidora Um", "email", "distribuidora-um@teste.com",
                        "telefone", "11999999999", "marcaId", marcaId))
                .when().post("/fornecedores").then().statusCode(201).extract().jsonPath().getLong("id");

        given().queryParam("marcaId", marcaId).when().get("/modelos")
                .then().statusCode(200).body("nome", hasItem("Modelo Um"), "size()", equalTo(1));
        given().queryParam("marcaId", marcaId).when().get("/fornecedores")
                .then().statusCode(200).body("nome", hasItem("Distribuidora Um"), "size()", equalTo(1));
        given().when().get("/marcas/{id}", marcaId).then().statusCode(200)
                .body("nome", equalTo("Marca Relacionamento"));
        given().contentType(ContentType.JSON).body(Map.of("nome", "Marca Relacionamento Atualizada"))
                .when().put("/marcas/{id}", marcaId).then().statusCode(200)
                .body("nome", equalTo("Marca Relacionamento Atualizada"));
        given().contentType(ContentType.JSON).body(Map.of("nome", "Modelo Um Atualizado", "marcaId", marcaId))
                .when().put("/modelos/{id}", modeloId).then().statusCode(200)
                .body("nome", equalTo("Modelo Um Atualizado"));
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Distribuidora Atualizada", "email", "distribuidora-um@teste.com",
                        "telefone", "11988888888", "marcaId", marcaId))
                .when().put("/fornecedores/{id}", fornecedorId).then().statusCode(200)
                .body("telefone", equalTo("11988888888"));

        long categoriaId = given().contentType(ContentType.JSON).body(Map.of("nome", "Categoria Relacionamento"))
                .when().post("/categorias").then().statusCode(201).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Produto Inconsistente", "preco", 10, "estoque", 1,
                        "categoriaId", categoriaId, "modeloId", outroModeloId, "fornecedorId", fornecedorId))
                .when().post("/produtos").then().statusCode(400)
                .body("mensagem", equalTo("Modelo e fornecedor devem pertencer a mesma marca."));
        long produtoId = given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Produto Relacionado", "preco", 10, "estoque", 1,
                        "categoriaId", categoriaId, "modeloId", modeloId, "fornecedorId", fornecedorId))
                .when().post("/produtos").then().statusCode(201)
                .body("marcaNome", equalTo("Marca Relacionamento Atualizada"))
                .extract().jsonPath().getLong("id");

        given().when().delete("/marcas/{id}", marcaId).then().statusCode(409);
        given().when().delete("/modelos/{id}", modeloId).then().statusCode(409);
        given().when().delete("/fornecedores/{id}", fornecedorId).then().statusCode(409);
        given().when().delete("/produtos/{id}", produtoId).then().statusCode(204);
        given().when().delete("/fornecedores/{id}", fornecedorId).then().statusCode(204);
        given().when().delete("/modelos/{id}", modeloId).then().statusCode(204);
        given().when().delete("/marcas/{id}", marcaId).then().statusCode(204);
        given().when().get("/marcas/{id}", marcaId).then().statusCode(404);
        given().when().delete("/modelos/{id}", outroModeloId).then().statusCode(204);
        given().when().delete("/marcas/{id}", outraMarcaId).then().statusCode(204);
    }
}
