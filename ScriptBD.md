## Criação da tabela produto
-- Table: public.produto

-- DROP TABLE IF EXISTS public.produto;

CREATE TABLE IF NOT EXISTS public.produto
(
    id bigint NOT NULL DEFAULT nextval('produto_id_seq'::regclass),
    hash uuid DEFAULT gen_random_uuid(),
    nome character varying(255) COLLATE pg_catalog."default" NOT NULL,
    descricao text COLLATE pg_catalog."default",
    ean13 character varying(13) COLLATE pg_catalog."default" NOT NULL,
    preco numeric(13,2) NOT NULL,
    quantidade numeric(13,2) NOT NULL,
    estoque_min numeric(13,2) NOT NULL,
    dtcreate timestamp with time zone,
    dtupdate timestamp with time zone,
    lativo boolean DEFAULT false,
    CONSTRAINT produto_pkey PRIMARY KEY (id),
    CONSTRAINT produto_ean13_key UNIQUE (ean13),
    CONSTRAINT produto_preco_check CHECK (preco >= 0::numeric),
    CONSTRAINT produto_quantidade_check CHECK (quantidade >= 0::numeric),
    CONSTRAINT produto_estoque_min_check CHECK (estoque_min >= 0::numeric)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.produto
    OWNER to postgres;
