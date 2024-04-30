import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Row, Col, Table } from 'reactstrap';
import { Translate, translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ISeason } from 'app/shared/model/season.model';
import { searchEntities, getEntities } from './season.reducer';

export const Season = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');

  const seasonList = useAppSelector(state => state.season.entities);
  const loading = useAppSelector(state => state.season.loading);

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
      <h2 id="season-heading" data-cy="SeasonHeading">
        <Translate contentKey="ofieAnimeApp.season.home.title">Seasons</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="ofieAnimeApp.season.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/season/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="ofieAnimeApp.season.home.createLabel">Create new Season</Translate>
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
                  placeholder={translate('ofieAnimeApp.season.home.search')}
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
        {seasonList && seasonList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.titleInJapan">Title In Japan</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.titleInEnglis">Title In Englis</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.relaseDate">Relase Date</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.addDate">Add Date</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.startDate">Start Date</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.endDate">End Date</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.avrgeEpisodeLength">Avrge Episode Length</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.type">Type</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.seasonType">Season Type</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.cover">Cover</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.anime">Anime</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.season.yearlySeason">Yearly Season</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {seasonList.map((season, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/season/${season.id}`} color="link" size="sm">
                      {season.id}
                    </Button>
                  </td>
                  <td>{season.titleInJapan}</td>
                  <td>{season.titleInEnglis}</td>
                  <td>{season.relaseDate ? <TextFormat type="date" value={season.relaseDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{season.addDate ? <TextFormat type="date" value={season.addDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{season.startDate ? <TextFormat type="date" value={season.startDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{season.endDate ? <TextFormat type="date" value={season.endDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{season.avrgeEpisodeLength}</td>
                  <td>
                    <Translate contentKey={`ofieAnimeApp.Type.${season.type}`} />
                  </td>
                  <td>
                    <Translate contentKey={`ofieAnimeApp.SeasonType.${season.seasonType}`} />
                  </td>
                  <td>{season.cover}</td>
                  <td>{season.anime ? <Link to={`/anime/${season.anime.id}`}>{season.anime.id}</Link> : ''}</td>
                  <td>
                    {season.yearlySeason ? <Link to={`/yearly-season/${season.yearlySeason.id}`}>{season.yearlySeason.id}</Link> : ''}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/season/${season.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/season/${season.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/season/${season.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
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
              <Translate contentKey="ofieAnimeApp.season.home.notFound">No Seasons found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Season;
