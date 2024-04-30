import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Row, Col, Table } from 'reactstrap';
import { byteSize, Translate, translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IAnime } from 'app/shared/model/anime.model';
import { searchEntities, getEntities } from './anime.reducer';

export const Anime = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');

  const animeList = useAppSelector(state => state.anime.entities);
  const loading = useAppSelector(state => state.anime.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const startSearching = e => {
    if (search) {
      dispatch(searchEntities({ query: search }));
    }
    e.preventDefault();
  };

  const clear = () => {
    setSearch('');
    dispatch(getEntities({}));
  };

  const handleSearch = event => setSearch(event.target.value);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="anime-heading" data-cy="AnimeHeading">
        <Translate contentKey="ofieAnimeApp.anime.home.title">Anime</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="ofieAnimeApp.anime.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/anime/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="ofieAnimeApp.anime.home.createLabel">Create new Anime</Translate>
          </Link>
        </div>
      </h2>
      <Row>
        <Col sm="12">
          <Form onSubmit={startSearching}>
            <FormGroup>
              <InputGroup>
                <Input
                  type="text"
                  name="search"
                  defaultValue={search}
                  onChange={handleSearch}
                  placeholder={translate('ofieAnimeApp.anime.home.search')}
                />
                <Button className="input-group-addon">
                  <FontAwesomeIcon icon="search" />
                </Button>
                <Button type="reset" className="input-group-addon" onClick={clear}>
                  <FontAwesomeIcon icon="trash" />
                </Button>
              </InputGroup>
            </FormGroup>
          </Form>
        </Col>
      </Row>
      <div className="table-responsive">
        {animeList && animeList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.title">Title</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.discription">Discription</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.cover">Cover</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.relaseDate">Relase Date</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.source">Source</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.studio">Studio</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.anime.favirote">Favirote</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {animeList.map((anime, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/anime/${anime.id}`} color="link" size="sm">
                      {anime.id}
                    </Button>
                  </td>
                  <td>{anime.title}</td>
                  <td>{anime.discription}</td>
                  <td>{anime.cover}</td>
                  <td>{anime.relaseDate ? <TextFormat type="date" value={anime.relaseDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{anime.source ? <Link to={`/source/${anime.source.id}`}>{anime.source.id}</Link> : ''}</td>
                  <td>{anime.studio ? <Link to={`/studio/${anime.studio.id}`}>{anime.studio.id}</Link> : ''}</td>
                  <td>{anime.favirote ? <Link to={`/favirote/${anime.favirote.id}`}>{anime.favirote.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/anime/${anime.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/anime/${anime.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/anime/${anime.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="ofieAnimeApp.anime.home.notFound">No Anime found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Anime;
