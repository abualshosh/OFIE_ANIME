import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './comment.reducer';

export const CommentDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const commentEntity = useAppSelector(state => state.comment.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="commentDetailsHeading">
          <Translate contentKey="ofieAnimeApp.comment.detail.title">Comment</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{commentEntity.id}</dd>
          <dt>
            <span id="comment">
              <Translate contentKey="ofieAnimeApp.comment.comment">Comment</Translate>
            </span>
          </dt>
          <dd>{commentEntity.comment}</dd>
          <dt>
            <span id="like">
              <Translate contentKey="ofieAnimeApp.comment.like">Like</Translate>
            </span>
          </dt>
          <dd>{commentEntity.like}</dd>
          <dt>
            <span id="disLike">
              <Translate contentKey="ofieAnimeApp.comment.disLike">Dis Like</Translate>
            </span>
          </dt>
          <dd>{commentEntity.disLike}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.comment.episode">Episode</Translate>
          </dt>
          <dd>{commentEntity.episode ? commentEntity.episode.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.comment.anime">Anime</Translate>
          </dt>
          <dd>{commentEntity.anime ? commentEntity.anime.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.comment.season">Season</Translate>
          </dt>
          <dd>{commentEntity.season ? commentEntity.season.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/comment" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/comment/${commentEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CommentDetail;
