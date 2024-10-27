CREATE TABLE IF NOT EXISTS comprobantes ( 
  ruc varchar(13),
  codigo char(8),
  secuencial int,
  edn CLOB,
  xml CLOB,
  tipo varchar(16),
  estado varchar(12),
  last_update long
);

CREATE TABLE IF NOT EXISTS contribuyentes ( 
  id int AUTO_INCREMENT PRIMARY KEY,
  ruc varchar(13),
  keystore CLOB,
  datos CLOB
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_comp_ruc_codigo ON comprobantes(ruc, tipo, codigo);
CREATE UNIQUE INDEX IF NOT EXISTS idx_comp_ruc_tipo_secuencial ON comprobantes(ruc, tipo, secuencial);
CREATE UNIQUE INDEX IF NOT EXISTS idx_contribuyentes_ruc ON contribuyentes(ruc);
