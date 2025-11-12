-- FP-001A - Aggregations SQL
-- File: .gemini/sprints/feature-plans/FP-001A-aggregations.sql
-- Contains the SQL queries used by TD-001A Dashboard Explanation feature

-- Query 1 - Totales generales
SELECT
  COUNT(DISTINCT maquinista_fk) AS total_operarios,
  COUNT(DISTINCT maquina_fk) AS total_maquinas,
  SUM(cantidad) AS produccion_total,
  SUM(peso_neto) AS peso_total_kg,
  MIN(fecha_contabilizacion) AS fecha_inicio,
  MAX(fecha_contabilizacion) AS fecha_fin,
  COUNT(DISTINCT numero_pallet) AS total_pallets
FROM public.fact_production fp
WHERE fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin;

-- Query 2 - Top operarios
SELECT
  dm.codigo_maquinista,
  dm.nombre_completo,
  COUNT(DISTINCT fp.fecha_contabilizacion) as dias_trabajados,
  SUM(fp.cantidad) as total_unidades,
  SUM(fp.peso_neto) as total_kg,
  ROUND(
    SUM(fp.cantidad)::numeric /
    NULLIF(COUNT(DISTINCT fp.fecha_contabilizacion), 0),
    2
  ) as promedio_unidades_por_dia
FROM public.fact_production fp
JOIN public.dim_maquinista dm ON fp.maquinista_fk = dm.id
WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY dm.id, dm.codigo_maquinista, dm.nombre_completo
ORDER BY total_unidades DESC
LIMIT 10;

-- Query 3 - Distribución por turno
SELECT turno, SUM(cantidad) AS unidades, COUNT(DISTINCT maquinista_fk) AS operarios
FROM public.fact_production
WHERE fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY turno;

-- Query 4 - Top máquinas
SELECT dmaq.codigo_maquina, dmaq.nombre_maquina, SUM(fp.cantidad) as total_unidades
FROM public.fact_production fp
JOIN public.dim_maquina dmaq ON fp.maquina_fk = dmaq.id
WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY dmaq.id, dmaq.codigo_maquina, dmaq.nombre_maquina
ORDER BY total_unidades DESC
LIMIT 5;

-- Query 5 - Tendencia diaria
SELECT fecha_contabilizacion, SUM(cantidad) as produccion_dia
FROM public.fact_production
WHERE fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY fecha_contabilizacion
ORDER BY fecha_contabilizacion;

