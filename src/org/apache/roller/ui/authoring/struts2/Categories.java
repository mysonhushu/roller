/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.ui.authoring.struts2;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogCategoryPathComparator;
import org.apache.roller.ui.core.util.struts2.UIAction;


/**
 * Manage weblog categories.
 */
public class Categories extends UIAction {
    
    private static Log log = LogFactory.getLog(Categories.class);
    
    // the id of the category we are viewing
    private String categoryId = null;
    
    // the category we are viewing
    private WeblogCategoryData category = null;
    
    // list of category ids to move
    private String[] selectedCategories = null;
    
    // category id of the category to move to
    private String targetCategoryId = null;
    
    // all categories from the action weblog
    private Set allCategories = Collections.EMPTY_SET;
    
    // path of categories representing selected categories hierarchy
    private List categoryPath = Collections.EMPTY_LIST;
    
    
    public Categories() {
        this.actionName = "categories";
        this.desiredMenu = "editor";
        this.pageTitle = "categoriesForm.rootTitle";
    }
    
    
    // author perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.AUTHOR;
    }
    
    
    public void myPrepare() {
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            if(!StringUtils.isEmpty(getCategoryId()) && 
                    !"/".equals(getCategoryId())) {
                setCategory(wmgr.getWeblogCategory(getCategoryId()));
            } else {
                setCategory(wmgr.getRootWeblogCategory(getActionWeblog()));
            }
        } catch (RollerException ex) {
            log.error("Error looking up category", ex);
        }
    }
    
    
    public String execute() {
        
        // build list of categories for display
        TreeSet allCategories = new TreeSet(new WeblogCategoryPathComparator());
        
        try {
            // Build list of all categories, except for current one, sorted by path.
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List<WeblogCategoryData> cats = wmgr.getWeblogCategories(getActionWeblog(), true);
            for(WeblogCategoryData cat : cats) {
                if (!cat.getId().equals(getCategoryId())) {
                    allCategories.add(cat);
                }
            }
            
            // build category path
            WeblogCategoryData parent = getCategory().getParent();
            if(parent != null) {
                List categoryPath = new LinkedList();
                categoryPath.add(0, getCategory());
                while (parent != null) {
                    categoryPath.add(0, parent);
                    parent = parent.getParent();
                }
                setCategoryPath(categoryPath);
            }
        } catch (RollerException ex) {
            log.error("Error building categories list", ex);
            // TODO: i18n
            addError("Error building categories list");
        }
        
        if (allCategories.size() > 0) {
            setAllCategories(allCategories);
        }
        
        return LIST;
    }
    
    
    public String move() {
        
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            
            log.debug("Moving categories to category - "+getTargetCategoryId());
            
            // Move subCategories to new category.
            String[] cats = getSelectedCategories();
            WeblogCategoryData parent = wmgr.getWeblogCategory(getTargetCategoryId());
            if(cats != null) {
                for (int i = 0; i < cats.length; i++) {
                    WeblogCategoryData cd =
                            wmgr.getWeblogCategory(cats[i]);
                    
                    // Don't move category into itself.
                    if (!cd.getId().equals(parent.getId()) && 
                            !parent.descendentOf(cd)) {
                        wmgr.moveWeblogCategory(cd, parent);
                    } else {
                        addMessage("categoriesForm.warn.notMoving", cd.getName());
                    }
                }
                
                // flush changes
                RollerFactory.getRoller().flush();
            }
            
        } catch (RollerException ex) {
            log.error("Error moving categories", ex);
            addError("categoriesForm.error.move");
        }
        
        return execute();
    }
    

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public WeblogCategoryData getCategory() {
        return category;
    }

    public void setCategory(WeblogCategoryData category) {
        this.category = category;
    }

    public String[] getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(String[] selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public String getTargetCategoryId() {
        return targetCategoryId;
    }

    public void setTargetCategoryId(String targetCategoryId) {
        this.targetCategoryId = targetCategoryId;
    }

    public Set getAllCategories() {
        return allCategories;
    }

    public void setAllCategories(Set allCategories) {
        this.allCategories = allCategories;
    }

    public List getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(List categoryPath) {
        this.categoryPath = categoryPath;
    }
    
}