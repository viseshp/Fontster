/*
 * Copyright 2015 Priyesh Patel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chromium.fontinstaller.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.chromium.fontinstaller.R;
import com.chromium.fontinstaller.ui.backuprestore.BackupRestoreFragment;
import com.chromium.fontinstaller.ui.common.BaseActivity;
import com.chromium.fontinstaller.ui.fontlist.FontListFragment;
import com.chromium.fontinstaller.ui.install.FontActivity;
import com.chromium.fontinstaller.ui.settings.SettingsActivity;
import com.chromium.fontinstaller.util.RootUtils;
import com.google.android.gms.ads.AdView;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnItemClick;


public class MainActivity extends BaseActivity implements MaterialSearchView.SearchViewListener {

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.drawer_list)
    ListView drawerList;

    @Bind(R.id.search_view)
    MaterialSearchView searchView;

    @Bind(R.id.adView)
    AdView adView;

    private ActionBarDrawerToggle drawerToggle;
    private FragmentManager fragmentManager;
    private FontListFragment fontListFragment;
    private BackupRestoreFragment backupRestoreFragment;

    private boolean shouldShowSearch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle("Fontster");

        initializeAd(adView);

        RootUtils.requestAccess();

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerList.setAdapter(new NavDrawerAdapter(this, generateNavItems()));

        fragmentManager = getSupportFragmentManager();
        fontListFragment = new FontListFragment();
        backupRestoreFragment = new BackupRestoreFragment();

        swapFragment(fontListFragment);

        final String[] fontList = getResources().getStringArray(R.array.font_list);
        searchView.setOnSearchViewListener(this);
        searchView.setSuggestionIcon(null);
        searchView.setSuggestions(fontList);
        searchView.setOnItemClickListener((parent, view, position, id) -> {
            final String fontName = getFontNameFromListItem(view);
            final Intent intent = FontActivity.getLaunchIntent(this, fontName);
            startActivity(intent);
        });
    }

    private String getFontNameFromListItem(View view) {
        return ((TextView) view.findViewById(R.id.suggestion_text)).getText().toString();
    }

    private Drawable getDrawableFromArray(int position, String... array) {
        return getResources().getDrawable(
                getResources().getIdentifier(array[position], "drawable", getPackageName()));
    }

    private ArrayList<NavDrawerItem> generateNavItems() {
        ArrayList<NavDrawerItem> items = new ArrayList<>(3);
        String[] titles = getResources().getStringArray(R.array.nav_drawer_titles);
        String[] icons = getResources().getStringArray(R.array.nav_drawer_icons);

        for (int i = 0; i < Math.min(titles.length, icons.length); i++)
            items.add(new NavDrawerItem(titles[i], getDrawableFromArray(i, icons)));

        return items;
    }

    private void swapFragment(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @SuppressWarnings("unused")
    @OnItemClick(R.id.drawer_list)
    public void onNavItemClicked(int position) {
        switch (position) {
            case 0:
                swapFragment(fontListFragment);
                drawerLayout.closeDrawers();
                break;
            case 1:
                swapFragment(backupRestoreFragment);
                drawerLayout.closeDrawers();
                break;
            case 2:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        shouldShowSearch = (position == 0);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(shouldShowSearch);
        searchView.setMenuItem(searchItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START) || searchView.isSearchOpen()) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSearchViewShown() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public void onSearchViewClosed() {

    }
}
