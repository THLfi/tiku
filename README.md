# THL reporting interface for precalculated cubes and reports 


## Development

### Configuration

The application requires a file `src/main/resources/jdbc.properties` with the following properties:

```
database.driver=org.postgresql.Driver
database.url=jdbc:postgresql://<host>:<port>/<database>
database.user=
database.password=
database.environment.schema=
```

The `database.environment.schema` property may be used differentiate between different environments (deve, test, prod). 
The data may have different states in each environment. 

### Running the software

To start the application in a development environment run ```mvn jetty:run``` in the project basedir.

Application should respond at `http://localhost:8080`

The application servers resources at `/{state}/{lang}/{subject}/{section}/` where
- state is one of deve, test, prod, see Concepts
- lang is an ISO-693-1 language code e.g. fi, sv, en
- subject (optional), see Concepts
- section (optional), see Concepts  

### Creating a WAR-file

To create a WAR-file run ```mvn package``` in the project basedir.

## Data

### Concepts

The application is based on a data interchange format used to provide performant multidimensional datasets
with flexible metadata that can be localized. The format combines the idea of OLAP cubes and RDF. 
The cubes are assumed to be precalculated.

The dataset consists of 
- one or more fact tables that contains all the measure values
- one table describing the hierarchy of each dimension used in the fact tables called tree
- one table describing the metadata of each dimension as RDF triples
- any number of reports that are based on fact tables

The backing database may contain any number of these datasets. The datasets are grouped by subjects to
support topical listings for a better UX. 

The dataset may have multiple versions. Each version is identified by a `runid` (timestamp) and has it's own
state. The state may be one of deve, test or prod. This allows checking new datasets and their updates in the
same instance and makes publishing a new dataset version fast.   

### Database schema

Tables describing available datasets include

- meta_hydra - lists all datasets
- meta_table - lists all database tables containing the data
- meta_state - lists the state of each dataset
- meta_ref_id_map - lists global surrogate ids for each node in metadata

Tables describing datasets include
- x<runid>_fact* - one for each fact table in a dataset
- x<runid>_tree - one for each dataset
- x<runid>_meta - one for each dataset

Tables for usage logging include
- user_log - for each access 
- user_log_seletion - for each filter value in access

### Fact tables

Fact tables consists of a column for each dimension, a column determining the measure and a column containing the value

| time_key | region_key | measure_key | val |
|---|---|---|---|---|
| 1 | 10 | 20 | 34.3 |
| 1 | 11 | 20 | 23.5 |
| 2 | 10 | 20 | 33.0 |
| 2 | 11 | 20 | 24.5 |

where 
- time_key contains a key in the tree table where dimension = time
- region_key contains a key in the tree table where dimension = region
- measure_key contains a key in the tree table where dimension = measure
- val is measure value that may be non-numeric

### Tree tables

| key | parent_key | dimension | stage | ref |
|---|---|---|---|---|
| | | | | |

where 

- key is a dataset unique surrogate for the dimension node
- parent_key references the parent of the dimension node unless node is at the root level
- dimension is the identifier of the dimension
- stage is the identifier of the hierarchy level e.g. year, month, week, day 
- ref is the uri of the dimension node

### Meta tables

| ref | tag | lang | data |
|---|---|---|---|
| | | | |

where 

- ref is the subject uri 
- tag is the predicate uri
- lang is a ISO 639-1 language code
- data is the object value (a literal or an uri)


### Metadata predicates

Supported metadata predicates include

- name
- sort
- decimal
- password
- is 


### Report description language

Reports (called summaries) are described using an XML based report description language. The language allows you
to define which dimensions are used as filters (either constant or interactive) and what kind of visualisations 
are shown to the user. 

Currently supported visualisations include
- tables
- line charts
- bar charts
- pie charts
- gauge charts
