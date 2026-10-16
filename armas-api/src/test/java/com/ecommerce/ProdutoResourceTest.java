package com.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class ProdutoResourceTest {

    @Test
    void deveExecutarCrudCompleto() {
        Long categoriaId = criarCategoria("Categoria CRUD");
        Long id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Produto de teste",
                          "descricao": "Criado pelo teste automatizado",
                          "preco": 1500.0,
                          "estoque": 4,
                          "categoriaId": %d
                        }
                        """.formatted(categoriaId))
                .when().post("/produtos")
                .then()
                .statusCode(201)
                .body("nome", equalTo("Produto de teste"))
                .body("categoriaNome", equalTo("Categoria CRUD"))
                .extract().jsonPath().getLong("id");

        given()
                .when().get("/produtos")
                .then()
                .statusCode(200)
                .body("items.nome", hasItem("Produto de teste"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Produto atualizado",
                          "descricao": "Atualizado pelo teste automatizado",
                          "preco": 1750.0,
                          "estoque": 6,
                          "categoriaId": %d
                        }
                        """.formatted(categoriaId))
                .when().put("/produtos/{id}", id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Produto atualizado"));

        given()
                .when().delete("/produtos/{id}", id)
                .then()
                .statusCode(204);

        given()
                .when().get("/produtos/{id}", id)
                .then()
                .statusCode(404)
                .body("mensagem", equalTo("Produto nao encontrado"));
    }

    @Test
    void deveRejeitarProdutoInvalido() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"", "preco":-1, "estoque":-1}
                        """)
                .when().post("/produtos")
                .then()
                .statusCode(400);
    }

    @Test
    void deveFiltrarPorNomeNoBancoEPaginar() {
        Long categoriaId = criarCategoria("Categoria Paginacao");
        long[] ids = new long[5];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = given()
                    .contentType(ContentType.JSON)
                    .body("""
                            {"nome":"Busca paginada %d", "preco":100, "estoque":1, "categoriaId":%d}
                            """.formatted(i, categoriaId))
                    .when().post("/produtos")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("id");
        }

        given().queryParam("nome", "BUSCA PAGINADA").queryParam("page", 0).queryParam("pageSize", 2)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("items", hasSize(2))
                .body("totalItems", equalTo(5))
                .body("page", equalTo(0));

        given().queryParam("nome", "busca paginada").queryParam("page", 2).queryParam("pageSize", 2)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("items", hasSize(1))
                .body("totalItems", equalTo(5));

        given().queryParam("page", -1)
                .when().get("/produtos")
                .then().statusCode(400);

        for (long id : ids) {
            given().when().delete("/produtos/{id}", id).then().statusCode(204);
        }
    }

    private Long criarCategoria(String nome) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"nome\":\"" + nome + "\"}")
                .when().post("/categorias")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
