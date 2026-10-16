import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';

@Injectable()
export class PaginadorPortugues extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Itens por página:';
  override nextPageLabel = 'Próxima página';
  override previousPageLabel = 'Página anterior';
  override firstPageLabel = 'Primeira página';
  override lastPageLabel = 'Última página';
  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (!length || !pageSize) return '0 de ' + length;
    const inicio = page * pageSize;
    return inicio + 1 + ' – ' + Math.min(inicio + pageSize, length) + ' de ' + length;
  };
}
