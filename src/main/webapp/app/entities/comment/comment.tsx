import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Row, Col, Table } from 'reactstrap';
import { byteSize, Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IComment } from 'app/shared/model/comment.model';
import { searchEntities, getEntities } from './comment.reducer';

export const Comment = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');

  const commentList = useAppSelector(state => state.comment.entities);
  const loading = useAppSelector(state => state.comment.loading);

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
      <h2 id="comment-heading" data-cy="CommentHeading">
        <Translate contentKey="ofieAnimeApp.comment.home.title">Comments</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="ofieAnimeApp.comment.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/comment/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="ofieAnimeApp.comment.home.createLabel">Create new Comment</Translate>
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
                  placeholder={translate('ofieAnimeApp.comment.home.search')}
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
        {commentList && commentList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.comment">Comment</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.like">Like</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.disLike">Dis Like</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.episode">Episode</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.anime">Anime</Translate>
                </th>
                <th>
                  <Translate contentKey="ofieAnimeApp.comment.season">Season</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {commentList.map((comment, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/comment/${comment.id}`} color="link" size="sm">
                      {comment.id}
                    </Button>
                  </td>
                  <td>{comment.comment}</td>
                  <td>{comment.like}</td>
                  <td>{comment.disLike}</td>
                  <td>{comment.episode ? <Link to={`/episode/${comment.episode.id}`}>{comment.episode.id}</Link> : ''}</td>
                  <td>{comment.anime ? <Link to={`/anime/${comment.anime.id}`}>{comment.anime.id}</Link> : ''}</td>
                  <td>{comment.season ? <Link to={`/season/${comment.season.id}`}>{comment.season.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/comment/${comment.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/comment/${comment.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/comment/${comment.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
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
              <Translate contentKey="ofieAnimeApp.comment.home.notFound">No Comments found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Comment;
