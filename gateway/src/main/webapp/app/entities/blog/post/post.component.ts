import { mixins } from 'vue-class-component';
import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IPost } from '@/shared/model/blog/post.model';

import JhiDataUtils from '@/shared/data/data-utils.service';

import PostService from './post.service';
import AlertService from '@/shared/alert/alert.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class Post extends mixins(JhiDataUtils) {
  @Inject('postService') private postService: () => PostService;
  @Inject('alertService') private alertService: () => AlertService;

  public currentSearch = '';
  private removeId: number = null;
  public itemsPerPage = 20;
  public queryCount: number = null;
  public page = 1;
  public previousPage = 1;
  public propOrder = 'id';
  public reverse = false;
  public totalItems = 0;

  public posts: IPost[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllPosts();
  }

  public search(query): void {
    if (!query) {
      return this.clear();
    }
    this.currentSearch = query;
    this.retrieveAllPosts();
  }

  public clear(): void {
    this.currentSearch = '';
    this.page = 1;
    this.retrieveAllPosts();
  }

  public retrieveAllPosts(): void {
    this.isFetching = true;
    const paginationQuery = {
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: this.sort(),
    };
    if (this.currentSearch) {
      this.postService()
        .search(this.currentSearch, paginationQuery)
        .then(
          res => {
            this.posts = res.data;
            this.totalItems = Number(res.headers['x-total-count']);
            this.queryCount = this.totalItems;
            this.isFetching = false;
          },
          err => {
            this.isFetching = false;
            this.alertService().showHttpError(this, err.response);
          }
        );
      return;
    }
    this.postService()
      .retrieve(paginationQuery)
      .then(
        res => {
          this.posts = res.data;
          this.totalItems = Number(res.headers['x-total-count']);
          this.queryCount = this.totalItems;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
          this.alertService().showHttpError(this, err.response);
        }
      );
  }

  public handleSyncList(): void {
    this.clear();
  }

  public prepareRemove(instance: IPost): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removePost(): void {
    this.postService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('blogApp.blogPost.deleted', { param: this.removeId });
        this.$bvToast.toast(message.toString(), {
          toaster: 'b-toaster-top-center',
          title: 'Info',
          variant: 'danger',
          solid: true,
          autoHideDelay: 5000,
        });
        this.removeId = null;
        this.retrieveAllPosts();
        this.closeDialog();
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public sort(): Array<any> {
    const result = [this.propOrder + ',' + (this.reverse ? 'desc' : 'asc')];
    if (this.propOrder !== 'id') {
      result.push('id');
    }
    return result;
  }

  public loadPage(page: number): void {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  public transition(): void {
    this.retrieveAllPosts();
  }

  public changeOrder(propOrder): void {
    this.propOrder = propOrder;
    this.reverse = !this.reverse;
    this.transition();
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
