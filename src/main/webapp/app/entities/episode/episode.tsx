import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Row, Col, Table } from 'reactstrap';
import { byteSize, Translate, translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IEpisode } from 'app/shared/model/episode.model';
import { searchEntities, getEntities } from './episode.reducer';

export const Episode = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');

  const episodeList = useAppSelector(state => state.episode.entities);
  const loading = useAppSelector(state => state.episode.loading);

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
      <h2 id="episode-heading" data-cy="EpisodeHeading">
        <Translate contentKey="ofieAnimeApp.episode.home.title">Episodes</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="ofieAnimeApp.episode.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/episode/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="ofieAnimeApp.episode.home.createLabel">Create new Episode</Translate>
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
                  placeholder={translate('ofieAnimeApp.episode.home.search')}
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
        {episodeList && episodeList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="ofieAnimeApp.episode.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.episode.title">Title</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.episode.episodeLink">Episode Link</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.episode.relaseDate">Relase Date</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.episode.history">History</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.episode.season">Season</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {episodeList.map((episode, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/episode/${episode.id}`} color="link" size="sm">
                      {episode.id}
                    </Button>
                  </td>
                  <td>{episode.title}</td>
                  <td>{episode.episodeLink}</td>
                  <td>
                    {episode.relaseDate ? <TextFormat type="date" value={episode.relaseDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                  </td>
                  <td>{episode.history ? <Link to={`/history/${episode.history.id}`}>{episode.history.id}</Link> : ''}</td>
                  <td>{episode.season ? <Link to={`/season/${episode.season.id}`}>{episode.season.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/episode/${episode.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/episode/${episode.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/episode/${episode.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
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
              <Translate contentKey="ofieAnimeApp.episode.home.notFound">No Episodes found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Episode;
