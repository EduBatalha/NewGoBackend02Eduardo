-- Criação da tabela produto
CREATE TABLE IF NOT EXISTS produto
(
    id SERIAL PRIMARY KEY,
    hash uuid DEFAULT gen_random_uuid(),
    nome character varying(255) NOT NULL,
    descricao text,
    ean13 character varying(13) NOT NULL,
    preco numeric(13,2) NOT NULL,
    quantidade numeric(13,2) NOT NULL,
    estoque_min numeric(13,2) NOT NULL,
    dtcreate timestamp with time zone DEFAULT now(),
    dtupdate timestamp with time zone,
    lativo boolean DEFAULT false,
    CONSTRAINT produto_ean13_key UNIQUE (ean13),
    CONSTRAINT produto_preco_check CHECK (preco >= 0::numeric),
    CONSTRAINT produto_quantidade_check CHECK (quantidade >= 0::numeric),
    CONSTRAINT produto_estoque_min_check CHECK (estoque_min >= 0::numeric)
);

-- Criação da função para atualizar o campo dtupdate
CREATE OR REPLACE FUNCTION update_dtupdate()
RETURNS TRIGGER AS $$
BEGIN
  NEW.dtupdate = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Associação do gatilho à tabela produto
CREATE TRIGGER produto_update_dtupdate
BEFORE UPDATE ON produto
FOR EACH ROW
EXECUTE FUNCTION update_dtupdate();

-- Definir permissões
ALTER TABLE produto OWNER TO postgres;
